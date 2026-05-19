package com.sparta.orderservice.order.presentation.dto;

import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// GET /orders, GET /orders/{order_id} 응답
public record OrderResponse(
        UUID orderId,
        UUID requesterCompanyId,
        UUID receiverCompanyId,
        String recipientName,
        String phone,
        String address,
        LocalDateTime dueDate,
        String requestMemo,
        BigDecimal totalPrice,
        BigDecimal deliveryFee,
        BigDecimal finalPrice,
        OrderStatus status,
        List<CompanyOrderSummary> companyOrders, // 해당 주문에 속한 업체별 주문 요약 목록
        LocalDateTime createdAt
) {

    // OrderResponse 안에 업체별 주문 요약 (OrderItem 목록 미포함)
    public record CompanyOrderSummary(
            UUID companyOrderId,
            UUID companyId,
            BigDecimal subtotalPrice,
            BigDecimal subtotalDeliveryFee,
            CompanyOrderStatus status
    ) {}

    public static OrderResponse from(Order order) {
        List<CompanyOrderSummary> companySummaries = order.getCompanyOrders().stream()
                .map(co -> new CompanyOrderSummary(
                        co.getCompanyOrderId(),
                        co.getCompanyId(),
                        co.getSubtotalPrice(),
                        co.getSubtotalDeliveryFee(),
                        co.getStatus()
                ))
                .toList();

        return new OrderResponse(
                order.getOrderId(),
                order.getRequesterCompanyId(),
                order.getReceiverCompanyId(),
                order.getRecipientName(),
                order.getPhone(),
                order.getAddress(),
                order.getDueDate(),
                order.getRequestMemo(),
                order.getTotalPrice(),
                order.getDeliveryFee(),
                order.getFinalPrice(),
                order.getStatus(),
                companySummaries,
                order.getCreatedAt()
        );
    }
}
