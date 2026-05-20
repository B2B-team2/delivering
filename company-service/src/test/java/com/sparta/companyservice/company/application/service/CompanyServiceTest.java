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
    @DisplayName("데이터 전달 검증: Command 객체의 데이터가 엔티티로 누락 없이 전달되는가?")
    void commandToEntityMappingTest() {
        // given
        CompanyCreateCommand command = CompanyCreateCommand.builder()
                .companyName("Mapping Test")
                .companyType("RECEIVER")
                .businessNumber("111-22-33333")
                .hubId(UUID.randomUUID())
                .latitude(35.0)
                .longitude(127.0)
                .phone("010-1234-5678")
                .description("Desc")
                .address("Seoul")
                .logoUrl("http://logo.com")
                .build();

        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        companyService.createCompany(command);

        // then
        verify(companyRepository).save(argThat(company -> 
            company.getCompanyName().equals(command.getCompanyName()) &&
            company.getCompanyType().equals(CompanyTypeEnum.RECEIVER) &&
            company.getBusinessNumber().equals(command.getBusinessNumber()) &&
            company.getHubId().equals(command.getHubId()) &&
            company.getLatitude().equals(command.getLatitude()) &&
            company.getLongitude().equals(command.getLongitude()) &&
            company.getPhone().equals(command.getPhone()) &&
            company.getDescription().equals(command.getDescription()) &&
            company.getAddress().equals(command.getAddress()) &&
            company.getLogoUrl().equals(command.getLogoUrl())
        ));
    }
}
