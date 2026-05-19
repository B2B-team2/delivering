package com.sparta.orderservice.order.presentation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(
        UUID requesterCompanyId,        // 요청(공급)업체
        UUID receiverCompanyId,         // 수령업체
        String recipientName,           // 수령인 실명
        String phone,                   // 수령인 연락처
        String slackId,                 // 수령인 Slack ID (nullable)
        String address,                 // JSON: {"address": "기본주소", "address_detail": "상세주소"}
        LocalDateTime dueDate,          // 납품 기한
        String requestMemo,             // 요청 사항 (nullable)
        List<CompanyOrderRequest> companyOrders
) {

    public record CompanyOrderRequest(
            UUID companyId,             // 공급업체 ID
            List<OrderItemRequest> orderItems
    ) {}

    public record OrderItemRequest(
            UUID productOptionId,       // 상품 옵션 ID
            int quantity,               // 구매 수량
            BigDecimal unitPrice        // 구매 시점 단가
    ) {}
}
