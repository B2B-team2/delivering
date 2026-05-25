package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.CompanyOrder;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyOrderJpaRepository extends JpaRepository<CompanyOrder, UUID> {

    // 단건 조회: orderItems를 JOIN으로 한 방에 로딩 (상태 전환 시 orderItems 접근 대비)
    @EntityGraph(attributePaths = {"orderItems"})
    Optional<CompanyOrder> findByCompanyOrderIdAndDeletedAtIsNull(UUID companyOrderId);
}
