package com.sparta.orderservice.payment.infrastructure.repository;

import com.sparta.orderservice.payment.domain.core.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository — infrastructure 계층
 * Domain Repository 인터페이스(PaymentRepository)의 구현 기반
 */
public interface PaymentJpaRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByPaymentIdAndDeletedAtIsNull(UUID paymentId);

    Optional<Payment> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    Page<Payment> findAllByDeletedAtIsNull(Pageable pageable);

    Page<Payment> findByReceiverCompanyIdAndDeletedAtIsNull(UUID receiverCompanyId, Pageable pageable);
}
