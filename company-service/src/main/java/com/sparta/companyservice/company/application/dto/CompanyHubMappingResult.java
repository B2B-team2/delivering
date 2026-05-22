package com.sparta.companyservice.company.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class CompanyHubMappingResult {

    private final List<MappingItem> mappings;

    @Getter
    @Builder
    public static class MappingItem {
        private final UUID companyId;
        private final UUID hubId;
        private final String companyName;
    }
}
