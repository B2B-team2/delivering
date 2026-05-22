package com.sparta.orderservice.payment.infrastructure;

import com.sparta.orderservice.order.application.service.OrderService;
import com.sparta.orderservice.payment.application.port.OrderQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * OrderQueryPort의 Adapter 구현체
 * OrderRepository 직접 참조 대신 OrderService를 통해 접근 → order 도메인의 내부 구현이 아닌 공개된 서비스 진입점만 사용
 */
@Component
@RequiredArgsConstructor
public class OrderQueryAdapter implements OrderQueryPort {

    private final OrderService orderService;

    @Override
    public boolean isCancellable(UUID orderId) {
        return orderService.isCancellable(orderId);
    }
}
