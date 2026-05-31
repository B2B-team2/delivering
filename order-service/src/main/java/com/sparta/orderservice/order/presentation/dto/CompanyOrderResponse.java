package com.sparta.orderservice.order.presentation.dto;

import com.sparta.orderservice.order.application.dto.CompanyOrderResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// GET /orders/company/{company_order_id} 응답
public record CompanyOrderResponse(
        UUID companyOrderId,
        UUID orderId,
        UUID companyId,
        BigDecimal subtotalPrice,
        BigDecimal subtotalDeliveryFee,
        String status,
        List<OrderItemSummary> orderItems
) {
    // CompanyOrderResponse 안에 포함되는 주문 항목 요약
    public record OrderItemSummary(
            UUID orderItemId,
            UUID productOptionId,
            Integer quantity,
            BigDecimal unitPrice
    ) {}

    // Application DTO(CompanyOrderResult)로부터 변환
    public static CompanyOrderResponse from(CompanyOrderResult result) {
        List<OrderItemSummary> items = result.orderItems().stream()
                .map(item -> new OrderItemSummary(
                        item.orderItemId(),
                        item.productOptionId(),
                        item.quantity(),
                        item.unitPrice()
                ))
                .toList();

        return new CompanyOrderResponse(
                result.companyOrderId(),
                result.orderId(),
                result.companyId(),
                result.subtotalPrice(),
                result.subtotalDeliveryFee(),
                result.status(),
                items
        );
    }
}
