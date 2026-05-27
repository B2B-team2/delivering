package com.sparta.orderservice.order.presentation.dto;

import java.util.List;
import java.util.UUID;

/**
 * 업체 주문 상세 정보 응답 (내부 API 전용)
 */
public record CompanyOrderDetailsResponse(
        UUID orderId,
        UUID companyOrderId,
        List<ItemDetails> items
) {
    public record ItemDetails(
            UUID productOptionId,
            int quantity
    ) {}
}
