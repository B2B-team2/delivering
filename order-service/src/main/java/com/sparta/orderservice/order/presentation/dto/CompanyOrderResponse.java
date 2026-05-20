package com.sparta.orderservice.order.presentation.dto;

import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.OrderItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// GET /orders/company/{company_order_id} 응답
public record CompanyOrderResponse(
        UUID companyOrderId,
        UUID companyId,
        BigDecimal subtotalPrice,
        BigDecimal subtotalDeliveryFee,
        CompanyOrderStatus status,
        List<OrderItemSummary> orderItems
) {

    // CompanyOrderResponse 안에 포함되는 주문 항목 요약
    public record OrderItemSummary(
            UUID orderItemId,
            UUID productOptionId,
            Integer quantity,
            BigDecimal unitPrice
    ) {
        public static OrderItemSummary from(OrderItem item) {
            return new OrderItemSummary(
                    item.getOrderItemId(),
                    item.getProductOptionId(),
                    item.getQuantity(),
                    item.getUnitPrice()
            );
        }
    }

    public static CompanyOrderResponse from(CompanyOrder co) {
        List<OrderItemSummary> items = co.getOrderItems().stream()
                .map(OrderItemSummary::from)
                .toList();
        return new CompanyOrderResponse(
                co.getCompanyOrderId(),
                co.getCompanyId(),
                co.getSubtotalPrice(),
                co.getSubtotalDeliveryFee(),
                co.getStatus(),
                items
        );
    }
}
