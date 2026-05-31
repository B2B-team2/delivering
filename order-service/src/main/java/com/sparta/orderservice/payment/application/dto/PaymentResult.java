package com.sparta.orderservice.payment.application.dto;

import com.sparta.orderservice.payment.domain.core.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Application 계층 응답 DTO — Service → Controller 전달용
 * enum → String 변환으로 Presentation 계층에 도메인 타입 노출 방지
 * (Presentation 에서 PaymentStatus, PaymentMethod 등 도메인 enum 을 직접 import 하지 않도록)
 */
public record PaymentResult(
        UUID paymentId,
        UUID orderId,
        String paymentMethod,   // PaymentMethod enum → String (도메인 타입 은닉)
        BigDecimal amount,
        String status,          // PaymentStatus enum → String (도메인 타입 은닉)
        String pgTransactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Payment 도메인 객체 → PaymentResult 변환
     */
    public static PaymentResult from(Payment payment) {
        return new PaymentResult(
                payment.getPaymentId(),
                payment.getOrderId(),
                payment.getPaymentMethod().name(),  // enum → String
                payment.getAmount(),
                payment.getStatus().name(),         // enum → String
                payment.getPgTransactionId(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
