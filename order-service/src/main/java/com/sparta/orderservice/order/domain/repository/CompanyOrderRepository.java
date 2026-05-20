package com.sparta.orderservice.order.domain.repository;

import com.sparta.orderservice.order.domain.core.CompanyOrder;

import java.util.Optional;
import java.util.UUID;

// 순수 자바 인터페이스 — 외부 의존성(Spring, JPA) 없음
// soft delete 필터링은 Infrastructure(CompanyOrderRepositoryImpl)에서 처리
public interface CompanyOrderRepository {

    Optional<CompanyOrder> findCompanyOrderById(UUID companyOrderId);
}
