package com.sparta.orderservice.payment.application.port;

import java.util.UUID;

/**
 * PaymentService가 Order 정보를 조회하기 위한 Outbound Port
 * PaymentService는 Order 도메인을 직접 알지 않고 이 인터페이스만 의존
 * 구현체(Adapter)는 payment/infrastructure에 위치
 */
public interface OrderQueryPort {

    boolean isCancellable(UUID orderId);
}
