package com.sparta.operationsservice.claim.application.service;

import com.sparta.operationsservice.claim.domain.core.ClaimStatus;
import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ClaimWriter {

    private final OrderClaimRepository orderClaimRepository;

    @Transactional
    public void commitClaimStatus(OrderClaim claim, ClaimStatus newStatus, BigDecimal refundAmount) {
        claim.updateStatusAndAmount(newStatus, refundAmount);
        orderClaimRepository.save(claim);
    }
}
