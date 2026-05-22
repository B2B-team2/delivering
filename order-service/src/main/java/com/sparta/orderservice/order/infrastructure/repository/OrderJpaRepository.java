package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);
}
