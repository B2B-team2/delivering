package com.sparta.orderservice.payment.domain.repository;

import com.sparta.orderservice.payment.domain.core.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * 순수 자바 인터페이스 — 외부 의존성(Spring, JPA) 없음
 * 구현체는 payment/infrastructure/repository/PaymentRepositoryImpl 에 위치
 * soft delete 필터링(deletedAt IS NULL)은 구현체에서 처리
 */
public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findPaymentById(UUID paymentId);

    Optional<Payment> findPaymentByOrderId(UUID orderId);

    Page<Payment> findAllPayments(Pageable pageable);

    // COMPANY_MANAGER용 — 수령업체 기준 결제 필터링 (반정규화 컬럼 직접 조회)
    Page<Payment> findPaymentsByReceiverCompanyId(UUID receiverCompanyId, Pageable pageable);
}
