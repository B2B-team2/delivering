package com.sparta.orderservice.payment.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 결제 생성 Command DTO
 * Presentation → Application 전달용 커맨드 객체
 * orderId는 PathVariable, paymentMethod·amount는 RequestBody에서 전달됨
 */
public record CreatePaymentCommand(
        UUID orderId,
        String paymentMethod,
        BigDecimal amount
) {
}
