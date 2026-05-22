package com.sparta.companyservice.company.presentation.dto;

import com.sparta.companyservice.company.application.dto.CompanyHubMappingResult;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class CompanyHubMappingResponse {

    private final Map<UUID, CompanyHubMappingDto> mappings;

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
    @Builder
    public static class CompanyHubMappingDto {
        private final UUID companyId;
        private final UUID hubId;
        private final String companyName;
    }
}
