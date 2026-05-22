package com.sparta.companyservice.company.presentation.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CompanyHubMappingRequest {

    @NotEmpty(message = "업체 ID 목록은 비어있을 수 없습니다.")
    private List<UUID> companyIds;
}
