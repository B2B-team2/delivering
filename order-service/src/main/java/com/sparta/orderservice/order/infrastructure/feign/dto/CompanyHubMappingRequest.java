package com.sparta.orderservice.order.infrastructure.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyHubMappingRequest {
    private List<UUID> companyIds;
}
