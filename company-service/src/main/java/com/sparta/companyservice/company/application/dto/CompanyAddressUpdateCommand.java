package com.sparta.companyservice.company.application.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CompanyAddressUpdateCommand {
    private String addressName;
    private String recipientName;
    private String phone;
    private String address;
    private String addressDetail;
    private String postalCode;
    private Boolean isDefault;
}
