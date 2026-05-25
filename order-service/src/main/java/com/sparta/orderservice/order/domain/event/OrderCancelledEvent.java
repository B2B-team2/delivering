package com.sparta.orderservice.order.domain.event;

import java.util.UUID;

/**
 * 주문 취소 도메인 이벤트
 * OrderService → PaymentEventHandler 로 결제 취소 위임
 * 같은 트랜잭션에서 실행 → 결제 취소 실패 시 주문 취소도 롤백
 */
public record OrderCancelledEvent(UUID orderId, UUID requesterId) {
}
