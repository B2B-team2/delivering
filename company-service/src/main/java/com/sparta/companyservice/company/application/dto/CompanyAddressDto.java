package com.sparta.companyservice.company.application.dto;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
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
public class CompanyAddressDto {
    private UUID addressId;
    private UUID companyId;
    private String addressName;
    private String recipientName;
    private String phone;
    private String address;
    private String addressDetail;
    private String postalCode;
    private Boolean isDefault;

    public static CompanyAddressDto from(CompanyDeliveryAddress entity) {
        return CompanyAddressDto.builder()
                .addressId(entity.getAddressId())
                .companyId(entity.getCompanyId())
                .addressName(entity.getAddressName())
                .recipientName(entity.getRecipientName())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .addressDetail(entity.getAddressDetail())
                .postalCode(entity.getPostalCode())
                .isDefault(entity.getIsDefault())
                .build();
    }
}
