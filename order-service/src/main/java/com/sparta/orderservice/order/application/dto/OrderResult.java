package com.sparta.orderservice.order.application.dto;

import com.sparta.orderservice.order.domain.core.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Application 계층 응답 DTO — Service → Controller 전달용
// Domain 엔티티(Order)를 직접 참조할 수 있는 계층
public record OrderResult(
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
        String status,                          // enum → String 변환 (Presentation 계층에 도메인 타입 노출 방지)
        List<CompanyOrderSummary> companyOrders,
        LocalDateTime createdAt
) {
    // 주문에 속한 업체별 주문 요약 (OrderItem 목록 미포함)
    public record CompanyOrderSummary(
            UUID companyOrderId,
            UUID companyId,
            BigDecimal subtotalPrice,
            BigDecimal subtotalDeliveryFee,
            String status                       // enum → String 변환
    ) {}

    public static OrderResult from(Order order) {
        List<CompanyOrderSummary> summaries = order.getCompanyOrders().stream()
                .map(co -> new CompanyOrderSummary(
                        co.getCompanyOrderId(),
                        co.getCompanyId(),
                        co.getSubtotalPrice(),
                        co.getSubtotalDeliveryFee(),
                        co.getStatus().name()
                ))
                .toList();

        return new OrderResult(
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
                order.getStatus().name(),
                summaries,
                order.getCreatedAt()
        );
    }
}
