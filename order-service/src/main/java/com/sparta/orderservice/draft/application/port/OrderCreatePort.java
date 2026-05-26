package com.sparta.orderservice.draft.application.port;

import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;

import java.util.UUID;

// Order 도메인으로의 주문 생성 요청 포트 (아키텍처 일관성 유지)
public interface OrderCreatePort {
    OrderResult createOrder(CreateOrderCommand command, UUID requesterId);
}
