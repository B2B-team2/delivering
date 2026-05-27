package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    // 목록 조회: 컬렉션은 default_batch_fetch_size(yml)로 IN 절 일괄 로딩
    Page<Order> findAllByDeletedAtIsNull(Pageable pageable);

    // 단건 조회: JOIN으로 companyOrders + orderItems 한 방에 로딩
    @EntityGraph(attributePaths = {"companyOrders", "companyOrders.orderItems"})
    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    // orderId → receiverCompanyId 단일 필드 조회 — 결제 접근 권한 검증용
    @Query("SELECT o.receiverCompanyId FROM Order o WHERE o.orderId = :orderId AND o.deletedAt IS NULL")
    Optional<UUID> findReceiverCompanyIdByOrderId(@Param("orderId") UUID orderId);

    // COMPANY_MANAGER용: 자기 회사가 수령업체 또는 공급업체로 참여한 주문 조회
    @Query(value = "SELECT DISTINCT o FROM Order o LEFT JOIN o.companyOrders co " +
                   "WHERE o.deletedAt IS NULL " +
                   "AND (o.receiverCompanyId = :companyId OR co.companyId = :companyId)",
           countQuery = "SELECT COUNT(DISTINCT o) FROM Order o LEFT JOIN o.companyOrders co " +
                        "WHERE o.deletedAt IS NULL " +
                        "AND (o.receiverCompanyId = :companyId OR co.companyId = :companyId)")
    Page<Order> findByCompanyIdAndDeletedAtIsNull(@Param("companyId") UUID companyId, Pageable pageable);
}
