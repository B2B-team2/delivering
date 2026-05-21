package com.sparta.orderservice.payment.presentation.dto;

import com.sparta.orderservice.payment.application.dto.PaymentResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 결제 API Response DTO — Presentation 계층
 * PaymentResult(Application DTO)를 받아 변환 — 도메인 타입(enum) 직접 참조 없음
 */
public record PaymentResponse(
        UUID paymentId,
        UUID orderId,
        String paymentMethod,
        BigDecimal amount,
        String status,
        String pgTransactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static PaymentResponse from(PaymentResult result) {
        return new PaymentResponse(
                result.paymentId(),
                result.orderId(),
                result.paymentMethod(),
                result.amount(),
                result.status(),
                result.pgTransactionId(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
