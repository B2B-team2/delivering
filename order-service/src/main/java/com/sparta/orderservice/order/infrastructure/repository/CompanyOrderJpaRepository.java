package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.CompanyOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyOrderJpaRepository extends JpaRepository<CompanyOrder, UUID> {

    @EntityGraph(attributePaths = {"orderItems", "order", "order.companyOrders"})
    Optional<CompanyOrder> findByCompanyOrderIdAndDeletedAtIsNull(UUID companyOrderId);
}
