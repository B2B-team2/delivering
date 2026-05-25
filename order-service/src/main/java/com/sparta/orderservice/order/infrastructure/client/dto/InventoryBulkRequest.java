package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

// Hub Service 재고 API 공통 요청 DTO (reserve / deduct / return)
// companyOrderId: reserve 시에만 사용, deduct/return은 null
public record InventoryBulkRequest(
        UUID orderId,
        UUID companyOrderId,
        List<InventoryItem> items
) {
}
