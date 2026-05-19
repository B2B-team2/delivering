package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
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
public class CompanyResponse {
    private UUID companyId;
    private String companyName;
    private CompanyTypeEnum companyType;
    private String phone;
    private String description;
    private String businessNumber;
    private UUID hubId;
    private Double latitude;
    private Double longitude;
    private String address;
    private String logoUrl;
    private LocalDateTime createdAt;

    public static CompanyResponse from(Company company) {
        return CompanyResponse.builder()
                .companyId(company.getCompanyId())
                .companyName(company.getCompanyName())
                .companyType(company.getCompanyType())
                .phone(company.getPhone())
                .description(company.getDescription())
                .businessNumber(company.getBusinessNumber())
                .hubId(company.getHubId())
                .latitude(company.getLatitude() != null ? company.getLatitude().getY() : null)
                .longitude(company.getLongitude() != null ? company.getLongitude().getX() : null)
                .address(company.getAddress())
                .logoUrl(company.getLogoUrl())
                .createdAt(company.getCreatedAt())
                .build();
    }
}
