package com.sparta.orderservice.payment.application.handler;

import com.sparta.orderservice.order.domain.event.OrderCreatedEvent;
import com.sparta.orderservice.payment.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 주문 이벤트 수신 → 결제 처리 위임
 * OrderService와 PaymentService 간 직접 의존 제거
 * @EventListener: 발행자와 같은 트랜잭션에서 실행 → 결제 실패 시 주문도 함께 롤백
 */
@Component
@RequiredArgsConstructor
public class PaymentEventHandler {

    private final PaymentService paymentService;

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        paymentService.createCompletedPayment(event.orderId(), event.totalPrice());
    }
}
