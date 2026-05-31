package com.sparta.companyservice.company.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CompanyUpdateCommand {
    private final String companyName;
    private final String companyType;
    private final String phone;
    private final String description;
    private final String businessNumber;
    private final UUID hubId;
    private final Double latitude;
    private final Double longitude;
    private final String address;
    private final String logoUrl;
}
