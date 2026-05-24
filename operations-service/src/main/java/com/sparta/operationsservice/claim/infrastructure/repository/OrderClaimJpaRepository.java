package com.sparta.operationsservice.claim.infrastructure.repository;

import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OrderClaimJpaRepository extends JpaRepository<OrderClaim, UUID> {
    Optional<OrderClaim> findByClaimIdAndDeletedAtIsNull(UUID claimId);
    Page<OrderClaim> findAllByDeletedAtIsNull(Pageable pageable);
    boolean existsByOrderItemIdAndDeletedAtIsNull(UUID orderItemId);
}
