package com.sparta.orderservice.payment.application.port;

import java.util.UUID;

/**
 * PaymentService가 주문 취소를 위임하기 위한 Outbound Port
 * PaymentService → OrderService 직접 의존을 방지 (순환 의존 방지)
 * 구현체(Adapter)는 payment/infrastructure에 위치
 */
public interface OrderCancelPort {

    void cancelOrder(UUID orderId, UUID requesterId);
}
