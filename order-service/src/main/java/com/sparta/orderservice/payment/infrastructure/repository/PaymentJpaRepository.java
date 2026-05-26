package com.sparta.orderservice.payment.infrastructure.repository;

import com.sparta.orderservice.payment.domain.core.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT p FROM Payment p WHERE p.orderId IN :orderIds AND p.deletedAt IS NULL")
    Page<Payment> findByOrderIdInAndDeletedAtIsNull(@Param("orderIds") List<UUID> orderIds, Pageable pageable);
}
