package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyAddressCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyAddressDto;
import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.domain.repository.CompanyDeliveryAddressRepository;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.port.CompanyQueryPort;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAddressServiceTest {

    @Mock
    private CompanyDeliveryAddressRepository companyDeliveryAddressRepository;

    @Mock
    private CompanyQueryPort companyQueryPort;

    @InjectMocks
    private CompanyAddressService companyAddressService;

    @Test
    @DisplayName("배송지 등록 성공")
    void registerAddressSuccessTest() {
        UUID companyId = UUID.randomUUID();
        CompanyAddressCreateCommand command = CompanyAddressCreateCommand.builder()
                .addressName("집")
                .recipientName("홍길동")
                .phone("010-1234-5678")
                .address("주소")
                .postalCode("12345")
                .isDefault(false)
                .build();

        CompanyDeliveryAddress savedAddress = CompanyDeliveryAddress.builder()
                .addressId(UUID.randomUUID())
                .companyId(companyId)
                .addressName(command.getAddressName())
                .recipientName(command.getRecipientName())
                .phone(command.getPhone())
                .address(command.getAddress())
                .postalCode(command.getPostalCode())
                .isDefault(command.getIsDefault())
                .build();

        when(companyQueryPort.existsCompanyById(companyId)).thenReturn(true);
        when(companyDeliveryAddressRepository.save(any(CompanyDeliveryAddress.class))).thenReturn(savedAddress);

        CompanyAddressDto result = companyAddressService.registerAddress(companyId, command);

        assertThat(result).isNotNull();
        assertThat(result.getAddressName()).isEqualTo(command.getAddressName());
        verify(companyDeliveryAddressRepository, times(1)).save(any(CompanyDeliveryAddress.class));
    }

    @Test
    @DisplayName("배송지 등록 성공: 기본 배송지 설정 시 기존 설정 false로 변경")
    void registerAddressSuccessWithDefaultTest() {
        UUID companyId = UUID.randomUUID();
        CompanyAddressCreateCommand command = CompanyAddressCreateCommand.builder()
                .addressName("집")
                .isDefault(true)
                .build();

        when(companyQueryPort.existsCompanyById(companyId)).thenReturn(true);
        when(companyDeliveryAddressRepository.save(any(CompanyDeliveryAddress.class)))
                .thenReturn(CompanyDeliveryAddress.builder().build());

        companyAddressService.registerAddress(companyId, command);

        verify(companyDeliveryAddressRepository, times(1)).updateAllIsDefaultFalseByCompanyId(companyId);
    }

    @Test
    @DisplayName("배송지 등록 실패: 존재하지 않는 업체")
    void registerAddressFail_CompanyNotFound() {
        UUID companyId = UUID.randomUUID();
        when(companyQueryPort.existsCompanyById(companyId)).thenReturn(false);

        assertThatThrownBy(() -> companyAddressService.registerAddress(companyId, any()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.COMPANY_NOT_FOUND.getMessage());
    }
}
