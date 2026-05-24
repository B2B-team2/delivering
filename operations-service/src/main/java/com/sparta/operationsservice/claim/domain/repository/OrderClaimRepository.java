package com.sparta.operationsservice.claim.domain.repository;

import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface OrderClaimRepository {
    OrderClaim save(OrderClaim claim);
    Optional<OrderClaim> findById(UUID claimId);
    Page<OrderClaim> findAll(Pageable pageable);
}
