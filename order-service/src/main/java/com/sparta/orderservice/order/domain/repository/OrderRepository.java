package com.sparta.orderservice.order.domain.repository;

import com.sparta.orderservice.order.domain.core.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 순수 자바 인터페이스 — 외부 의존성(Spring, JPA) 없음
// soft delete 필터링은 Infrastructure(OrderRepositoryImpl)에서 처리
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findOrderById(UUID orderId);

    List<Order> findAllOrders();
}
