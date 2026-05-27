package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
    public Page<Order> findAllOrders(Pageable pageable) {
        return orderJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public Page<Order> findOrdersByCompanyId(UUID companyId, Pageable pageable) {
        return orderJpaRepository.findByCompanyIdAndDeletedAtIsNull(companyId, pageable);
    }

    @Override
    public Optional<UUID> findReceiverCompanyIdByOrderId(UUID orderId) {
        return orderJpaRepository.findReceiverCompanyIdByOrderId(orderId);
    }

}
