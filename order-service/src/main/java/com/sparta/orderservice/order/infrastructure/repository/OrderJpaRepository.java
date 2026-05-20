package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    List<Order> findAllByDeletedAtIsNull();

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);
}
