package com.sparta.companyservice.company.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.company.application.dto.CompanyAddressCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyAddressDto;
import com.sparta.companyservice.company.application.dto.CompanyAddressUpdateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDefaultAddressDto;
import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.domain.repository.CompanyDeliveryAddressRepository;
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
        // given
        UUID companyId = UUID.randomUUID();
        CompanyAddressCreateCommand command = CompanyAddressCreateCommand.builder()
                .addressName("집")
                .recipientName("홍길동")
                .phone("010-1234-5678")
                .address("서울시")
                .isDefault(true)
                .build();

        CompanyDeliveryAddress savedAddress = CompanyDeliveryAddress.builder()
                .addressId(UUID.randomUUID())
                .companyId(companyId)
                .addressName(command.getAddressName())
                .isDefault(command.getIsDefault())
                .build();

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(companyDeliveryAddressRepository.save(any(CompanyDeliveryAddress.class))).thenReturn(savedAddress);

        // when
        CompanyAddressDto result = companyAddressService.registerAddress(companyId, command);

        // then
        assertThat(result.getAddressName()).isEqualTo(command.getAddressName());
        verify(companyDeliveryAddressRepository, times(1)).updateAllIsDefaultFalseByCompanyId(companyId);
        verify(companyDeliveryAddressRepository, times(1)).save(any(CompanyDeliveryAddress.class));
    }

    @Test
    @DisplayName("배송지 목록 조회 성공")
    void getAddressesSuccessTest() {
        // given
        UUID companyId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .addressId(UUID.randomUUID())
                .companyId(companyId)
                .addressName("회사")
                .build();

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(companyDeliveryAddressRepository.findAllByCompanyIdAndDeletedAtIsNull(companyId, pageable))
                .thenReturn(new PageImpl<>(List.of(address)));

        // when
        Page<CompanyAddressDto> result = companyAddressService.getAddresses(companyId, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getAddressName()).isEqualTo("회사");
    }

    @Test
    @DisplayName("배송지 삭제 성공")
    void deleteAddressSuccessTest() {
        // given
        UUID addressId = UUID.randomUUID();
        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .addressId(addressId)
                .build();

        when(companyDeliveryAddressRepository.findById(addressId)).thenReturn(Optional.of(address));

        // when
        CompanyAddressDto result = companyAddressService.deleteAddress(addressId, UUID.randomUUID());

        // then
        assertThat(result.getAddressId()).isEqualTo(addressId);
        verify(companyDeliveryAddressRepository, times(1)).findById(addressId);
    }

    @Test
    @DisplayName("기본 배송지 조회 성공")
    void getDefaultAddressSuccessTest() {
        // given
        UUID companyId = UUID.randomUUID();
        CompanyDeliveryAddress address = CompanyDeliveryAddress.builder()
                .companyId(companyId)
                .address("경기도 수원시")
                .addressDetail("3층")
                .recipientName("홍길동")
                .phone("010-1234-5678")
                .isDefault(true)
                .build();

        when(companyDeliveryAddressRepository.findDefaultAddressByCompanyId(companyId)).thenReturn(Optional.of(address));

        // when
        CompanyDefaultAddressDto result = companyAddressService.getDefaultAddress(companyId);

        // then
        assertThat(result.getAddress()).isEqualTo("경기도 수원시");
        assertThat(result.getRecipientName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("기본 배송지 조회 실패: 설정된 기본 배송지 없음")
    void getDefaultAddressFailTest() {
        // given
        UUID companyId = UUID.randomUUID();
        when(companyDeliveryAddressRepository.findDefaultAddressByCompanyId(companyId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyAddressService.getDefaultAddress(companyId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", CompanyErrorCode.ADDRESS_NOT_FOUND);
    }
}
