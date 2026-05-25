package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.UUID;

// Hub Service 재고 예약 취소 요청
public record StockCancelRequest(
        UUID orderId
) {
}
