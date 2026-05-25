package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.Map;
import java.util.UUID;

// Company Service 허브 매핑 일괄 조회 응답 DTO
// mappings: { "companyId(UUID 문자열)": { companyId, hubId, companyName } }
public record HubMappingResponse(
        Map<String, CompanyHubInfo> mappings
) {
    public record CompanyHubInfo(
            UUID companyId,
            UUID hubId,
            String companyName
    ) {
    }
}
