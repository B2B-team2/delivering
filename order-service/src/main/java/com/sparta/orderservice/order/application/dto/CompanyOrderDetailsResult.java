package com.sparta.orderservice.order.application.dto;

import java.util.List;
import java.util.UUID;

/**
 * 업체 주문 상세 정보 응답 (내부 API 전용)
 * Application 계층에서 관리하여 Presentation 계층과의 의존성 규칙 준수
 */
public record CompanyOrderDetailsResult(
        UUID orderId,
        UUID companyOrderId,
        List<ItemDetails> items
) {
    public record ItemDetails(
            UUID productOptionId,
            int quantity
    ) {}
}
