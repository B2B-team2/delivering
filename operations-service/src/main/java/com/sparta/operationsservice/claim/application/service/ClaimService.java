package com.sparta.operationsservice.claim.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.operationsservice.claim.application.dto.ClaimCreateCommand;
import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import com.sparta.operationsservice.claim.application.dto.ClaimStatusUpdateCommand;
import com.sparta.operationsservice.claim.domain.core.ClaimStatus;
import com.sparta.operationsservice.claim.domain.core.ClaimType;
import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import com.sparta.operationsservice.global.exception.OperationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaimService {

    private final OrderClaimRepository orderClaimRepository;

    @Transactional
    public ClaimDto createClaim(ClaimCreateCommand command) {
        if (orderClaimRepository.existsByOrderItemId(command.getOrderItemId())) {
            throw new BusinessException(OperationErrorCode.DUPLICATE_CLAIM);
        }

        OrderClaim claim = OrderClaim.builder()
                .orderItemId(command.getOrderItemId())
                .claimType(ClaimType.valueOf(command.getClaimType()))
                .reason(command.getReason())
                .refundAmount(command.getRefundAmount())
                .build();

        return ClaimDto.from(orderClaimRepository.save(claim));
    }

    public Page<ClaimDto> getClaims(Pageable pageable) {
        return orderClaimRepository.findAll(pageable)
                .map(ClaimDto::from);
    }

    public ClaimDto getClaim(UUID claimId) {
        OrderClaim claim = orderClaimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(OperationErrorCode.CLAIM_NOT_FOUND));
        return ClaimDto.from(claim);
    }

    @Transactional
    public ClaimDto updateClaimStatus(UUID claimId, ClaimStatusUpdateCommand command) {
        OrderClaim claim = orderClaimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(OperationErrorCode.CLAIM_NOT_FOUND));

        try {
            claim.updateStatusAndAmount(
                    ClaimStatus.valueOf(command.getStatus()),
                    command.getRefundAmount()
            );
        } catch (IllegalArgumentException e) {
            throw new BusinessException(OperationErrorCode.INVALID_CLAIM_STATUS);
        }

        return ClaimDto.from(claim);
    }
}
