package com.sparta.orderservice.payment.infrastructure;

import com.sparta.orderservice.order.application.service.OrderService;
import com.sparta.orderservice.payment.application.port.OrderCancelPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * OrderCancelPort 구현체 — infrastructure 계층
 * PaymentService가 OrderService를 직접 참조하지 않고 Port 인터페이스를 통해 위임
 */
@Component
@RequiredArgsConstructor
public class OrderCancelAdapter implements OrderCancelPort {

    private final OrderService orderService;

    @Override
    public void cancelOrder(UUID orderId, UUID requesterId) {
        orderService.cancelOrder(orderId, requesterId);
    }
}
