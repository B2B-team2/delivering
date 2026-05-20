package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {

    // JPA 구현체 — 이 클래스 내부에서만 사용
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public Optional<Order> findOrderById(UUID orderId) {
        return orderJpaRepository.findByOrderIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public List<Order> findAllOrders() {
        return orderJpaRepository.findAllByDeletedAtIsNull();
    }
}
