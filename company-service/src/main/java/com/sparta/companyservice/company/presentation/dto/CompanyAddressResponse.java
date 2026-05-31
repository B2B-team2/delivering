package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyAddressDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanyAddressResponse {
    private UUID addressId;
    private UUID companyId;
    private String addressName;
    private String recipientName;
    private String phone;
    private String address;
    private String addressDetail;
    private String postalCode;
    private Boolean isDefault;

    public static CompanyAddressResponse from(CompanyAddressDto dto) {
        return CompanyAddressResponse.builder()
                .addressId(dto.getAddressId())
                .companyId(dto.getCompanyId())
                .addressName(dto.getAddressName())
                .recipientName(dto.getRecipientName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .addressDetail(dto.getAddressDetail())
                .postalCode(dto.getPostalCode())
                .isDefault(dto.getIsDefault())
                .build();
    }
}
