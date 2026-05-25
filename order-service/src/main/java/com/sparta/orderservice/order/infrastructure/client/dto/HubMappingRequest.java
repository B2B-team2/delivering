package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

// Company Service 허브 매핑 일괄 조회 요청 DTO
public record HubMappingRequest(
        List<UUID> companyIds
) {
}
