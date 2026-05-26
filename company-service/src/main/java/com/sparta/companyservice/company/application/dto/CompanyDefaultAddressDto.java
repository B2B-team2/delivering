package com.sparta.companyservice.company.application.dto;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanyDefaultAddressDto {
    private String address;
    private String addressDetail;
    private String recipientName;
    private String phone;

    public static CompanyDefaultAddressDto from(CompanyDeliveryAddress entity) {
        return CompanyDefaultAddressDto.builder()
                .address(entity.getAddress())
                .addressDetail(entity.getAddressDetail())
                .recipientName(entity.getRecipientName())
                .phone(entity.getPhone())
                .build();
    }
}
