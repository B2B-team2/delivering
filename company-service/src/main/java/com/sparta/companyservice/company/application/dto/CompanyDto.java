package com.sparta.companyservice.company.application.dto;

import com.sparta.companyservice.company.domain.core.Company;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CompanyDto {
    private final UUID companyId;
    private final String companyName;
    private final String companyType; // Domain Enum 대신 String 반환
    private final String phone;
    private final String description;
    private final String businessNumber;
    private final UUID hubId;
    private final Double latitude;
    private final Double longitude;
    private final String address;
    private final String logoUrl;
    private final LocalDateTime createdAt;
    private final LocalDateTime deletedAt;

    public static CompanyDto from(Company company) {
        return CompanyDto.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyType(company.getCompanyType().name())
                .phone(company.getPhone())
                .description(company.getDescription())
                .businessNumber(company.getBusinessNumber())
                .hubId(company.getHubId())
                .latitude(company.getLocation() != null ? company.getLocation().getY() : null)
                .longitude(company.getLocation() != null ? company.getLocation().getX() : null)
                .address(company.getAddress())
                .logoUrl(company.getLogoUrl())
                .createdAt(company.getCreatedAt())
                .deletedAt(company.getDeletedAt())
                .build();
    }
}
