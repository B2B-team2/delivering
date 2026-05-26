package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyDeliveryAddressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDeliveryAddressResponse {
    private UUID addressId;
    private UUID companyId;
    private String addressName;
    private String recipientName;
    private String phone;
    private String address;
    private String addressDetail;
    private String postalCode;
    private Boolean isDefault;
    private LocalDateTime createdAt;

    public static CompanyDeliveryAddressResponse from(CompanyDeliveryAddressDto dto) {
        return CompanyDeliveryAddressResponse.builder()
                .addressId(dto.getAddressId())
                .companyId(dto.getCompanyId())
                .addressName(dto.getAddressName())
                .recipientName(dto.getRecipientName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .addressDetail(dto.getAddressDetail())
                .postalCode(dto.getPostalCode())
                .isDefault(dto.getIsDefault())
                .createdAt(dto.getCreatedAt())
                .build();
    }
}
