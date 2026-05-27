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
import com.sparta.operationsservice.claim.application.port.HubPort;
import com.sparta.operationsservice.claim.application.port.OrderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaimService {

    private final OrderClaimRepository orderClaimRepository;
    private final OrderPort orderPort;
    private final HubPort hubPort;

    @Transactional
    public ClaimDto createClaim(ClaimCreateCommand command) {
        if (orderClaimRepository.existsByCompanyOrderId(command.getCompanyOrderId())) {
            throw new BusinessException(OperationErrorCode.DUPLICATE_CLAIM);
        }

        OrderClaim claim = OrderClaim.builder()
                .companyOrderId(command.getCompanyOrderId())
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
    public ClaimDto updateClaimStatus(UUID claimId, ClaimStatusUpdateCommand command, UUID adminId) {
        OrderClaim claim = orderClaimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(OperationErrorCode.CLAIM_NOT_FOUND));

        ClaimStatus newStatus;
        try {
            newStatus = ClaimStatus.valueOf(command.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(OperationErrorCode.INVALID_CLAIM_STATUS);
        }

        // 상태가 PROCESSING으로 변경되는 경우 (승인)
        if (newStatus == ClaimStatus.PROCESSING && claim.getStatus() != ClaimStatus.PROCESSING) {
            processClaimApproval(claim, adminId);
        }

        claim.updateStatusAndAmount(newStatus, command.getRefundAmount());

        return ClaimDto.from(claim);
    }

    private void processClaimApproval(OrderClaim claim, UUID adminId) {
        // 1. Order Service에서 상위 orderId 및 상품 목록 조회 (via Port)
        OrderPort.CompanyOrderDetails details = orderPort.getCompanyOrderDetails(claim.getCompanyOrderId());

        // 2. Hub Service를 통해 재고 일괄 복원 (via Port)
        hubPort.returnStock(
                details.orderId(),
                details.items().stream()
                        .map(item -> new HubPort.InventoryItem(item.productOptionId(), item.quantity()))
                        .collect(Collectors.toList())
        );
        // 3. Order Service에서 주문 상태 변경 (via Port)
        orderPort.cancelCompanyOrderByClaim(claim.getCompanyOrderId(), adminId);
    }
}
