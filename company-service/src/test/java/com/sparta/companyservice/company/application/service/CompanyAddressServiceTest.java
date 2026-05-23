package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyAddressCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyAddressDto;
import com.sparta.companyservice.company.application.dto.CompanyAddressUpdateCommand;
import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.domain.repository.CompanyDeliveryAddressRepository;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.company.presentation.dto.CompanyAddressDeleteResponse;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyAddressServiceTest {

    @Mock
    private CompanyDeliveryAddressRepository companyDeliveryAddressRepository;

    @Mock
    private CompanyRepository companyRepository;

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

        when(companyRepository.existsById(companyId)).thenReturn(true);
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

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(companyDeliveryAddressRepository.save(any(CompanyDeliveryAddress.class)))
                .thenReturn(CompanyDeliveryAddress.builder().build());

        companyAddressService.registerAddress(companyId, command);

        verify(companyDeliveryAddressRepository, times(1)).updateAllIsDefaultFalseByCompanyId(companyId);
    }

    @Test
    @DisplayName("배송지 등록 실패: 존재하지 않는 업체")
    void registerAddressFail_CompanyNotFound() {
        UUID companyId = UUID.randomUUID();
        when(companyRepository.existsById(companyId)).thenReturn(false);

        assertThatThrownBy(() -> companyAddressService.registerAddress(companyId, any()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.COMPANY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배송지 목록 조회 성공")
    void getAddressesSuccessTest() {
        UUID companyId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        CompanyDeliveryAddress address1 = CompanyDeliveryAddress.builder()
                .addressName("기본배송지")
                .isDefault(true)
                .build();
        CompanyDeliveryAddress address2 = CompanyDeliveryAddress.builder()
                .addressName("일반배송지")
                .isDefault(false)
                .build();

        Page<CompanyDeliveryAddress> addressPage = new PageImpl<>(List.of(address1, address2));

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(companyDeliveryAddressRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId, pageable))
                .thenReturn(addressPage);

        Page<CompanyAddressDto> result = companyAddressService.getAddresses(companyId, pageable);

        assertThat(result).hasSize(2);
        assertThat(result.getContent().get(0).getIsDefault()).isTrue();
        assertThat(result.getContent().get(1).getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("배송지 목록 조회 실패: 존재하지 않는 업체")
    void getAddressesFail_CompanyNotFound() {
        UUID companyId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(companyRepository.existsById(companyId)).thenReturn(false);

        assertThatThrownBy(() -> companyAddressService.getAddresses(companyId, pageable))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.COMPANY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteAddressSuccessTest() {
        // given
        UUID addressId = UUID.randomUUID();
        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .addressId(addressId)
                .build();

        when(companyDeliveryAddressRepository.findById(addressId)).thenReturn(java.util.Optional.of(address));

        // when
        CompanyAddressDeleteResponse result = companyAddressService.deleteAddress(addressId, "system");

        // then
        assertThat(result.getAddressId()).isEqualTo(addressId);
        assertThat(result.getDeletedAt()).isNotNull();
        verify(companyDeliveryAddressRepository, times(1)).findById(addressId);
    }

    @Test
    @DisplayName("배송지 삭제 실패: 존재하지 않는 배송지")
    void deleteAddressFail_AddressNotFound() {
        // given
        UUID addressId = UUID.randomUUID();
        when(companyDeliveryAddressRepository.findById(addressId)).thenReturn(java.util.Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyAddressService.deleteAddress(addressId, "system"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.ADDRESS_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배송지 수정 성공: 일반 정보 수정")
    void updateAddressSuccessTest() {
        // given
        UUID addressId = UUID.randomUUID();
        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .addressId(addressId)
                .addressName("기존 이름")
                .isDefault(false)
                .build();

        CompanyAddressUpdateCommand command = CompanyAddressUpdateCommand.builder()
                .addressName("수정된 이름")
                .isDefault(false)
                .build();

        when(companyDeliveryAddressRepository.findById(addressId)).thenReturn(java.util.Optional.of(address));

        // when
        CompanyAddressDto result = companyAddressService.updateAddress(addressId, command);

        // then
        assertThat(result.getAddressName()).isEqualTo("수정된 이름");
        verify(companyDeliveryAddressRepository, never()).updateAllIsDefaultFalseByCompanyId(any());
    }

    @Test
    @DisplayName("배송지 수정 성공: 기본 배송지로 변경 시 기존 설정 해제 호출 확인")
    void updateAddressDefaultChangeTest() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .addressId(addressId)
                .companyId(companyId)
                .isDefault(false)
                .build();

        CompanyAddressUpdateCommand command = CompanyAddressUpdateCommand.builder()
                .isDefault(true)
                .build();

        when(companyDeliveryAddressRepository.findById(addressId)).thenReturn(java.util.Optional.of(address));

        // when
        companyAddressService.updateAddress(addressId, command);

        // then
        verify(companyDeliveryAddressRepository, times(1)).updateAllIsDefaultFalseByCompanyId(companyId);
    }
}
