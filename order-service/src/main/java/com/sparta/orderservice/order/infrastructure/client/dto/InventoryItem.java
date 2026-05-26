package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.UUID;

// Hub Service 재고 API의 items 항목
public record InventoryItem(
        UUID productOptionId,
        int quantity
) {
}
