package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

// Saga 보상 전용: 배송 일괄 취소 요청 DTO
public record DeliveryCancelRequest(
        List<UUID> companyOrderIds
) {
}
