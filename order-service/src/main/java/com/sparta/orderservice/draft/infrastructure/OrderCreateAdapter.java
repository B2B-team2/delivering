package com.sparta.orderservice.draft.infrastructure;

import com.sparta.orderservice.draft.application.port.OrderCreatePort;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.application.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * OrderCreatePort 구현체 — infrastructure 계층
 * DraftService가 OrderService를 직접 참조하지 않고 Port 인터페이스를 통해 위임
 */
@Component
@RequiredArgsConstructor
public class OrderCreateAdapter implements OrderCreatePort {

    private final OrderService orderService;

    @Override
    public OrderResult createOrder(CreateOrderCommand command, UUID requesterId) {
        return orderService.createOrder(command, requesterId);
    }
}
