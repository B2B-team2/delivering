package com.sparta.companyservice.company.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyDeliveryAddressCreateCommand {
    private UUID companyId;
    private String addressName;
    private String recipientName;
    private String phone;
    private String address;
    private String addressDetail;
    private String postalCode;
    private Boolean isDefault;
}
