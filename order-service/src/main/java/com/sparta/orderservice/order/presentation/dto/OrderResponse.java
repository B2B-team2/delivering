package com.sparta.orderservice.order.presentation.dto;

import com.sparta.orderservice.order.application.dto.OrderResult;

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
        String status,
        List<CompanyOrderSummary> companyOrders, // 해당 주문에 속한 업체별 주문 요약 목록
        LocalDateTime createdAt
) {
    // OrderResponse 안에 업체별 주문 요약 (OrderItem 목록 미포함)
    public record CompanyOrderSummary(
            UUID companyOrderId,
            UUID companyId,
            BigDecimal subtotalPrice,
            BigDecimal subtotalDeliveryFee,
            String status
    ) {}

    // Application DTO(OrderResult)로부터 변환
    public static OrderResponse from(OrderResult result) {
        List<CompanyOrderSummary> summaries = result.companyOrders().stream()
                .map(co -> new CompanyOrderSummary(
                        co.companyOrderId(),
                        co.companyId(),
                        co.subtotalPrice(),
                        co.subtotalDeliveryFee(),
                        co.status()
                ))
                .toList();

        return new OrderResponse(
                result.orderId(),
                result.requesterCompanyId(),
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
