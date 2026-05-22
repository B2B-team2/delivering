package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyDto;
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
    private String companyType; // String으로 반환
    private String phone;
    private String description;
    private String businessNumber;
    private UUID hubId;
    private Double latitude;
    private Double longitude;
    private String address;
    private String logoUrl;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    public static CompanyResponse from(CompanyDto dto) {
        return CompanyResponse.builder()
                .companyId(dto.getCompanyId())
                .companyName(dto.getCompanyName())
                .companyType(dto.getCompanyType())
                .phone(dto.getPhone())
                .description(dto.getDescription())
                .businessNumber(dto.getBusinessNumber())
                .hubId(dto.getHubId())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .address(dto.getAddress())
                .logoUrl(dto.getLogoUrl())
                .createdAt(dto.getCreatedAt())
                .deletedAt(dto.getDeletedAt())
                .build();
    }
}
