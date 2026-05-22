package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDeliveryAddressCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.application.dto.CompanyHubMappingResult;
import com.sparta.companyservice.company.application.dto.CompanyUpdateCommand;
import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.domain.repository.CompanyDeliveryAddressRepository;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyDeliveryAddressRepository companyDeliveryAddressRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("업체 생성 성공: 유효한 명령 수신 시 Repository의 save가 호출되고 결과가 DTO로 변환되는가?")
    void createCompanySuccessTest() {
        // given
        CompanyCreateCommand command = CompanyCreateCommand.builder()
                .companyName("Test Company")
                .companyType("PRODUCER")
                .businessNumber("123-45-67890")
                .hubId(UUID.randomUUID())
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        Company savedCompany = Company.builder()
                .companyId(UUID.randomUUID())
                .companyName(command.getCompanyName())
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber(command.getBusinessNumber())
                .hubId(command.getHubId())
                .latitude(command.getLatitude())
                .longitude(command.getLongitude())
                .build();

        when(companyRepository.findByBusinessNumberAnyStatus(command.getBusinessNumber())).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        // when
        CompanyDto result = companyService.createCompany(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCompanyName()).isEqualTo(command.getCompanyName());
        verify(companyRepository, times(1)).save(any(Company.class));
    }

    @Test
    @DisplayName("잘못된 타입 예외 발생: 정의되지 않은 CompanyType 문자열이 올 경우 INVALID_COMPANY_TYPE 예외가 발생하는가?")
    void createCompanyInvalidTypeTest() {
        // given
        CompanyCreateCommand command = CompanyCreateCommand.builder()
                .companyType("INVALID_TYPE")
                .build();

        // when & then
        assertThatThrownBy(() -> companyService.createCompany(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.INVALID_COMPANY_TYPE);

        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("업체 생성 실패: 이미 활성 상태인 사업자 번호일 경우 DUPLICATE_BUSINESS_NUMBER 예외가 발생하는가?")
    void createCompanyDuplicateBusinessNumberActiveTest() {
        // given
        CompanyCreateCommand command = CompanyCreateCommand.builder()
                .companyType("PRODUCER")
                .businessNumber("123-45-67890")
                .build();

        Company activeCompany = Company.builder()
                .businessNumber(command.getBusinessNumber())
                .build();
        // deletedAt is null by default

        when(companyRepository.findByBusinessNumberAnyStatus(command.getBusinessNumber())).thenReturn(Optional.of(activeCompany));

        // when & then
        assertThatThrownBy(() -> companyService.createCompany(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.DUPLICATE_BUSINESS_NUMBER);

        assertThat(CompanyErrorCode.DUPLICATE_BUSINESS_NUMBER.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
        verify(companyRepository, never()).save(any());
    }

    @Test
    @DisplayName("업체 생성(복구) 성공: 삭제된 상태인 사업자 번호일 경우 데이터를 복구하고 업데이트하는가?")
    void createCompanyRestoreTest() {
        // given
        CompanyCreateCommand command = CompanyCreateCommand.builder()
                .companyName("New Company Name")
                .companyType("PRODUCER")
                .businessNumber("123-45-67890")
                .hubId(UUID.randomUUID())
                .build();

        Company deletedCompany = spy(Company.builder()
                .companyId(UUID.randomUUID())
                .businessNumber(command.getBusinessNumber())
                .build());
        deletedCompany.softDelete("user"); // soft delete

        when(companyRepository.findByBusinessNumberAnyStatus(command.getBusinessNumber())).thenReturn(Optional.of(deletedCompany));

        // when
        CompanyDto result = companyService.createCompany(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCompanyName()).isEqualTo(command.getCompanyName());
        assertThat(deletedCompany.getDeletedAt()).isNull();
        verify(deletedCompany).restore();
        verify(deletedCompany).update(anyString(), any(), any(), any(), anyString(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("업체 생성 실패: 저장 시 중복된 사업자 번호로 인한 제약 조건 위반 발생 시 DUPLICATE_BUSINESS_NUMBER 예외가 발생하는가?")
    void createCompanyDuplicateBusinessNumberConcurrencyTest() {
        // given
        CompanyCreateCommand command = CompanyCreateCommand.builder()
                .companyName("Test Company")
                .companyType("PRODUCER")
                .businessNumber("123-45-67890")
                .hubId(UUID.randomUUID())
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        when(companyRepository.findByBusinessNumberAnyStatus(command.getBusinessNumber())).thenReturn(Optional.empty());
        when(companyRepository.save(any(Company.class))).thenThrow(DataIntegrityViolationException.class);

        // when & then
        assertThatThrownBy(() -> companyService.createCompany(command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.DUPLICATE_BUSINESS_NUMBER);
    }

    @Test
    @DisplayName("업체 정보 수정 성공: 정상적인 데이터로 수정 시 필드값이 올바르게 변경되는가?")
    void updateCompanySuccessTest() {
        // given
        UUID companyId = UUID.randomUUID();
        Company existingCompany = spy(Company.builder()
                .companyId(companyId)
                .companyName("Old Name")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber("111-11-11111")
                .hubId(UUID.randomUUID())
                .build());

        CompanyUpdateCommand command = CompanyUpdateCommand.builder()
                .companyName("New Name")
                .companyType("PRODUCER")
                .businessNumber("222-22-22222")
                .hubId(UUID.randomUUID())
                .latitude(37.0)
                .longitude(127.0)
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(companyRepository.findByBusinessNumberAnyStatus("222-22-22222")).thenReturn(Optional.empty());

        // when
        CompanyDto result = companyService.updateCompany(companyId, command);

        // then
        assertThat(result.getCompanyName()).isEqualTo("New Name");
        assertThat(result.getBusinessNumber()).isEqualTo("222-22-22222");
        verify(existingCompany).update(eq("New Name"), eq(CompanyTypeEnum.PRODUCER), any(), any(), eq("222-22-22222"), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("업체 정보 수정 실패: 존재하지 않는 ID로 수정 시도 시 COMPANY_NOT_FOUND 예외가 발생하는가?")
    void updateCompanyNotFoundTest() {
        // given
        UUID companyId = UUID.randomUUID();
        CompanyUpdateCommand command = CompanyUpdateCommand.builder()
                .companyType("PRODUCER")
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyService.updateCompany(companyId, command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.COMPANY_NOT_FOUND);
    }

    @Test
    @DisplayName("업체 정보 수정 실패: 이미 존재하는 다른 업체의 사업자 번호로 변경 시도 시 DUPLICATE_BUSINESS_NUMBER 예외가 발생하는가?")
    void updateCompanyDuplicateBusinessNumberTest() {
        // given
        UUID companyId = UUID.randomUUID();
        Company existingCompany = Company.builder()
                .companyId(companyId)
                .companyName("Old Name")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber("111-11-11111")
                .build();

        CompanyUpdateCommand command = CompanyUpdateCommand.builder()
                .companyType("PRODUCER")
                .businessNumber("222-22-22222")
                .build();

        Company otherCompany = Company.builder().businessNumber("222-22-22222").build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(companyRepository.findByBusinessNumberAnyStatus("222-22-22222")).thenReturn(Optional.of(otherCompany));

        // when & then
        assertThatThrownBy(() -> companyService.updateCompany(companyId, command))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.DUPLICATE_BUSINESS_NUMBER);
    }

    @Test
    @DisplayName("업체 목록 조회 성공: Repository에서 반환된 Page<Company>가 Page<CompanyDto>로 정확히 변환되는가?")
    void getCompaniesSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Company company = Company.builder()
                .companyId(UUID.randomUUID())
                .companyName("Test Company")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber("123-45-67890")
                .hubId(UUID.randomUUID())
                .build();
        
        Page<Company> companyPage = new PageImpl<>(List.of(company), pageable, 1);
        when(companyRepository.findAll(any(Pageable.class))).thenReturn(companyPage);

        // when
        Page<CompanyDto> result = companyService.getCompanies(pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCompanyName()).isEqualTo(company.getCompanyName());
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(companyRepository).findAll(pageable);
    }

    @Test
    @DisplayName("업체 상세 조회 성공: 존재하는 ID로 조회 시 정확한 DTO가 반환되는가?")
    void getCompanySuccessTest() {
        // given
        UUID companyId = UUID.randomUUID();
        Company company = Company.builder()
                .companyId(companyId)
                .companyName("Detail Test Company")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber("123-45-67890")
                .build();

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when
        CompanyDto result = companyService.getCompany(companyId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getCompanyId()).isEqualTo(companyId);
        assertThat(result.getCompanyName()).isEqualTo(company.getCompanyName());
    }

    @Test
    @DisplayName("업체 상세 조회 실패: 존재하지 않는 ID로 조회 시 COMPANY_NOT_FOUND 예외가 발생하는가?")
    void getCompanyNotFoundTest() {
        // given
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyService.getCompany(companyId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.COMPANY_NOT_FOUND);
    }

    @Test
    @DisplayName("업체 삭제 성공: 존재하는 ID로 삭제 시 softDelete가 호출되고 deletedAt이 설정되는가?")
    void deleteCompanySuccessTest() {
        // given
        UUID companyId = UUID.randomUUID();
        String username = "testUser";
        Company company = spy(Company.builder()
                .companyId(companyId)
                .companyName("Delete Target")
                .companyType(CompanyTypeEnum.PRODUCER)
                .build());

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        // when
        CompanyDto result = companyService.deleteCompany(companyId, username);

        // then
        assertThat(result.getDeletedAt()).isNotNull();
        verify(company).softDelete(username);
    }

    @Test
    @DisplayName("업체 삭제 실패: 존재하지 않는 ID로 삭제 시도 시 COMPANY_NOT_FOUND 예외가 발생하는가?")
    void deleteCompanyNotFoundTest() {
        // given
        UUID companyId = UUID.randomUUID();
        String username = "testUser";
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyService.deleteCompany(companyId, username))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.COMPANY_NOT_FOUND);
    }

    @Test
    @DisplayName("업체 존재 여부 확인: 존재하는 ID면 true, 없으면 false를 반환하는가?")
    void existsCompanyTest() {
        // given
        UUID existingId = UUID.randomUUID();
        UUID nonExistingId = UUID.randomUUID();
        
        Company company = Company.builder().companyId(existingId).build();
        
        when(companyRepository.findById(existingId)).thenReturn(Optional.of(company));
        when(companyRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        // when
        boolean exists = companyService.existsCompany(existingId);
        boolean notExists = companyService.existsCompany(nonExistingId);

        // then
        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("업체-허브 매핑 조회 성공: 유효한 ID 목록 전달 시 정확한 결과가 반환되는가?")
    void getHubMappingsSuccessTest() {
        // given
        UUID companyId1 = UUID.randomUUID();
        UUID companyId2 = UUID.randomUUID();
        UUID hubId1 = UUID.randomUUID();
        UUID hubId2 = UUID.randomUUID();

        Company company1 = Company.builder()
                .companyId(companyId1)
                .companyName("Company 1")
                .hubId(hubId1)
                .build();
        Company company2 = Company.builder()
                .companyId(companyId2)
                .companyName("Company 2")
                .hubId(hubId2)
                .build();

        when(companyRepository.findAllByCompanyIdIn(List.of(companyId1, companyId2)))
                .thenReturn(List.of(company1, company2));

        // when
        CompanyHubMappingResult result = companyService.getHubMappings(List.of(companyId1, companyId2));

        // then
        assertThat(result.getMappings()).hasSize(2);
        assertThat(result.getMappings()).extracting("companyId")
                .containsExactlyInAnyOrder(companyId1, companyId2);
        assertThat(result.getMappings()).extracting("companyName")
                .containsExactlyInAnyOrder("Company 1", "Company 2");
    }

    @Test
    @DisplayName("업체-허브 매핑 조회 실패: 전달된 모든 ID가 유효하지 않을 경우 COMPANY_NOT_FOUND 예외가 발생하는가?")
    void getHubMappingsNotFoundTest() {
        // given
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());
        when(companyRepository.findAllByCompanyIdIn(ids)).thenReturn(List.of());

        // when & then
        assertThatThrownBy(() -> companyService.getHubMappings(ids))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.COMPANY_NOT_FOUND);
    }

    @Test
    @DisplayName("업체-허브 매핑 조회 일부 성공: 존재하는 업체에 대해서만 매핑 정보가 반환되는가?")
    void getHubMappingsPartialSuccessTest() {
        // given
        UUID validId = UUID.randomUUID();
        UUID invalidId = UUID.randomUUID();
        List<UUID> ids = List.of(validId, invalidId);

        Company company = Company.builder()
                .companyId(validId)
                .companyName("Valid Company")
                .hubId(UUID.randomUUID())
                .build();

        when(companyRepository.findAllByCompanyIdIn(ids)).thenReturn(List.of(company));

        // when
        CompanyHubMappingResult result = companyService.getHubMappings(ids);

        // then
        assertThat(result.getMappings()).hasSize(1);
        assertThat(result.getMappings().get(0).getCompanyId()).isEqualTo(validId);
    }

    @Test
    @DisplayName("허브 소속 업체 존재 확인: 업체가 존재할 경우 true를 반환하는가?")
    void existsCompanyInHubTrueTest() {
        // given
        UUID hubId = UUID.randomUUID();
        when(companyRepository.existsByHubId(hubId)).thenReturn(true);

        // when
        boolean exists = companyService.existsCompanyInHub(hubId);

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("허브 소속 업체 존재 확인: 업체가 존재하지 않을 경우 false를 반환하는가?")
    void existsCompanyInHubFalseTest() {
        // given
        UUID hubId = UUID.randomUUID();
        when(companyRepository.existsByHubId(hubId)).thenReturn(false);

        // when
        boolean exists = companyService.existsCompanyInHub(hubId);

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("배송지 등록 성공: 기본 배송지 설정 시 기존 설정 해제 로직이 호출되는가?")
    void createAddressWithDefaultTest() {
        // given
        UUID companyId = UUID.randomUUID();
        CompanyDeliveryAddressCreateCommand command = CompanyDeliveryAddressCreateCommand.builder()
                .companyId(companyId)
                .addressName("New Default Address")
                .isDefault(true)
                .build();

        Company company = Company.builder().companyId(companyId).build();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        CompanyDeliveryAddress savedAddress = CompanyDeliveryAddress.builder()
                .addressId(UUID.randomUUID())
                .companyId(companyId)
                .addressName(command.getAddressName())
                .isDefault(true)
                .build();
        when(companyDeliveryAddressRepository.save(any(CompanyDeliveryAddress.class))).thenReturn(savedAddress);

        // when
        companyService.createAddress(command);

        // then
        // 현재 CompanyService.createAddress 에는 이 호출 로직이 없으므로 verify 에서 실패할 것입니다.
        verify(companyDeliveryAddressRepository, times(1)).updateAllIsDefaultFalseByCompanyId(companyId);
    }
}
