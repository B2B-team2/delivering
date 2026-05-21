package com.sparta.orderservice.payment.infrastructure.repository;

import com.sparta.orderservice.payment.domain.core.Payment;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain Repository 구현체 — infrastructure 계층
 * PaymentRepository(순수 자바 인터페이스)를 구현하여 JPA 세부 사항을 캡슐화
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Payment save(Payment payment) {
        return paymentJpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findPaymentById(UUID paymentId) {
        // soft delete 필터링: deletedAt IS NULL 인 결제만 조회
        return paymentJpaRepository.findByPaymentIdAndDeletedAtIsNull(paymentId);
    }

    @Override
    public Page<Payment> findAllPayments(Pageable pageable) {
        // soft delete 필터링: deletedAt IS NULL 인 결제 목록 조회
        return paymentJpaRepository.findAllByDeletedAtIsNull(pageable);
    }
}
