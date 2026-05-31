package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyHubMappingResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyHubMappingResponse {

    private Map<UUID, CompanyHubMappingDto> mappings;

    public static CompanyHubMappingResponse from(CompanyHubMappingResult result) {
        Map<UUID, CompanyHubMappingDto> mappingMap = result.getMappings().stream()
                .collect(Collectors.toMap(
                        CompanyHubMappingResult.MappingItem::getCompanyId,
                        item -> CompanyHubMappingDto.builder()
                                .companyId(item.getCompanyId())
                                .hubId(item.getHubId())
                                .companyName(item.getCompanyName())
                                .build()
                ));

        return CompanyHubMappingResponse.builder()
                .mappings(mappingMap)
                .build();
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompanyHubMappingDto {
        private UUID companyId;
        private UUID hubId;
        private String companyName;
    }
}
