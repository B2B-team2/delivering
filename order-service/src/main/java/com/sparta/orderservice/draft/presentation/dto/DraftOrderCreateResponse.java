package com.sparta.orderservice.draft.presentation.dto;

import com.sparta.orderservice.order.application.dto.OrderResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// POST /drafts/orders (임시주문 → 주문 생성) 응답 DTO
// draft.presentation 계층이 order.application.dto(OrderResult)만 참조하도록 함
// → order.presentation.dto.OrderResponse 직접 참조를 제거하여 크로스 도메인 Presentation 의존성 제거
public record DraftOrderCreateResponse(
        UUID orderId,
        UUID receiverCompanyId,
        String recipientName,
        String phone,
        String address,
        LocalDateTime dueDate,
        String requestMemo,
        BigDecimal totalPrice,
        BigDecimal deliveryFee,
        BigDecimal finalPrice,
        String status,
        List<CompanyOrderSummary> companyOrders,
        LocalDateTime createdAt
) {
    public record CompanyOrderSummary(
            UUID companyOrderId,
            UUID companyId,
            BigDecimal subtotalPrice,
            BigDecimal subtotalDeliveryFee,
            String status
    ) {}

    public static DraftOrderCreateResponse from(OrderResult result) {
        List<CompanyOrderSummary> summaries = result.companyOrders().stream()
                .map(co -> new CompanyOrderSummary(
                        co.companyOrderId(),
                        co.companyId(),
                        co.subtotalPrice(),
                        co.subtotalDeliveryFee(),
                        co.status()
                ))
                .toList();

        return new DraftOrderCreateResponse(
                result.orderId(),
                result.receiverCompanyId(),
                result.recipientName(),
                result.phone(),
                result.address(),
                result.dueDate(),
                result.requestMemo(),
                result.totalPrice(),
                result.deliveryFee(),
                result.finalPrice(),
                result.status(),
                summaries,
                result.createdAt()
        );
    }
}
