package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.UUID;

// Hub Service 재고 예약 부분 취소 요청 (CompanyOrder 단위)
public record StockPartialCancelRequest(
        UUID companyOrderId
) {
}
