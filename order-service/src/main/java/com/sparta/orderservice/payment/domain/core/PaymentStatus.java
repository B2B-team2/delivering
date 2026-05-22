package com.sparta.orderservice.payment.domain.core;

public enum PaymentStatus {
    COMPLETED,  // 결제 완료 (선결제: 주문 생성 시 즉시 확정)
    CANCELLED   // 결제 취소
}
