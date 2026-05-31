package com.sparta.orderservice.order.domain.event;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 주문 생성 도메인 이벤트
 * OrderService → PaymentEventHandler 로 결제 처리 위임
 */
public record OrderCreatedEvent(UUID orderId, UUID receiverCompanyId, BigDecimal totalPrice) {
}
