package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

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
}
