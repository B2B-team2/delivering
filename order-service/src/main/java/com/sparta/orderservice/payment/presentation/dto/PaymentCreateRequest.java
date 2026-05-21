package com.sparta.orderservice.payment.presentation.dto;

import com.sparta.orderservice.payment.application.dto.CreatePaymentCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 결제 생성 API Request DTO — Presentation 계층
 * orderId는 PathVariable로 받으므로 toCommand(orderId) 형태로 변환
 */
public record PaymentCreateRequest(

        @NotNull(message = "결제 수단은 필수입니다.")
        String paymentMethod,   // CARD (현재는 카드 결제만 허용)

        @NotNull(message = "결제 금액은 필수입니다.")
        @Positive(message = "결제 금액은 0보다 커야 합니다.")
        BigDecimal amount

) {
    public CreatePaymentCommand toCommand(UUID orderId) {
        return new CreatePaymentCommand(orderId, paymentMethod, amount);
    }
}
