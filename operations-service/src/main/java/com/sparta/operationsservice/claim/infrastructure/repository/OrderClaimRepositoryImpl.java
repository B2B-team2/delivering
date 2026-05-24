package com.sparta.operationsservice.claim.infrastructure.repository;

import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderClaimRepositoryImpl implements OrderClaimRepository {

    private final OrderClaimJpaRepository orderClaimJpaRepository;

    @Override
    public OrderClaim save(OrderClaim claim) {
        return orderClaimJpaRepository.save(claim);
    }

    @Override
    public Optional<OrderClaim> findById(UUID claimId) {
        return orderClaimJpaRepository.findByClaimIdAndDeletedAtIsNull(claimId);
    }

    @Override
    public Page<OrderClaim> findAll(Pageable pageable) {
        return orderClaimJpaRepository.findAllByDeletedAtIsNull(pageable);
    }
}
