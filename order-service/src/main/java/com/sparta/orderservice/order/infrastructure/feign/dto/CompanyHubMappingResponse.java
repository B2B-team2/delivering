package com.sparta.orderservice.order.infrastructure.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyHubMappingResponse {
    private Map<UUID, CompanyHubMappingDto> mappings;

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
