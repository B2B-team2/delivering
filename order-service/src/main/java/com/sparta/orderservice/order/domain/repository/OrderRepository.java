package com.sparta.orderservice.order.domain.repository;

import com.sparta.orderservice.order.domain.core.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

// 순수 자바 인터페이스 — 외부 의존성(Spring, JPA) 없음
// soft delete 필터링은 Infrastructure(OrderRepositoryImpl)에서 처리
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findOrderById(UUID orderId);

    // 마스터용 - 전체 조회
    Page<Order> findAllOrders(Pageable pageable);

    // 업체 담당자용 - 소속 회사가 requester 또는 receiver인 주문
    Page<Order> findOrdersByCompanyId(UUID companyId, Pageable pageable);

    // 결제 접근 권한 검증용 — orderId → receiverCompanyId 단일 필드 조회
    Optional<UUID> findReceiverCompanyIdByOrderId(UUID orderId);

}
