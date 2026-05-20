package com.sparta.orderservice.order.application.dto;

import com.sparta.orderservice.order.domain.core.CompanyOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Application 계층 응답 DTO — Service → Controller 전달용
// Domain 엔티티(CompanyOrder)를 직접 참조할 수 있는 계층
public record CompanyOrderResult(
        UUID companyOrderId,
        UUID companyId,
        BigDecimal subtotalPrice,
        BigDecimal subtotalDeliveryFee,
        String status,                  // enum → String 변환 (Presentation 계층에 도메인 타입 노출 방지)
        List<OrderItemSummary> orderItems
) {
    // 주문 항목 요약
    public record OrderItemSummary(
            UUID orderItemId,
            UUID productOptionId,
            Integer quantity,
            BigDecimal unitPrice
    ) {}

    public static CompanyOrderResult from(CompanyOrder co) {
        List<OrderItemSummary> items = co.getOrderItems().stream()
                .map(item -> new OrderItemSummary(
                        item.getOrderItemId(),
                        item.getProductOptionId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new CompanyOrderResult(
                co.getCompanyOrderId(),
                co.getCompanyId(),
                co.getSubtotalPrice(),
                co.getSubtotalDeliveryFee(),
                co.getStatus().name(),
                items
        );
    }
}
