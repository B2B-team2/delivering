package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    // 목록 조회: 컬렉션은 default_batch_fetch_size(yml)로 IN 절 일괄 로딩
    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);

    // 단건 조회: JOIN으로 companyOrders + orderItems 한 방에 로딩
    @EntityGraph(attributePaths = {"companyOrders", "companyOrders.orderItems"})
    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);
}
