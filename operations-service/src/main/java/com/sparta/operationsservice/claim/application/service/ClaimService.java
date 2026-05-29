package com.sparta.operationsservice.claim.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.operationsservice.claim.application.dto.ClaimCreateCommand;
import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import com.sparta.operationsservice.claim.application.dto.ClaimStatusUpdateCommand;
import com.sparta.operationsservice.claim.application.port.HubPort;
import com.sparta.operationsservice.claim.application.port.OrderPort;
import com.sparta.operationsservice.claim.domain.core.ClaimStatus;
import com.sparta.operationsservice.claim.domain.core.ClaimType;
import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import com.sparta.operationsservice.global.exception.OperationErrorCode;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Deque;
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
    private final ClaimWriter claimWriter;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

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

    /**
     * 클레임 상태 변경 (SAGA Orchestrator)
     * 외부 서비스 호출 시 DB 커넥션을 점유하지 않도록 NOT_SUPPORTED 사용
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ClaimDto updateClaimStatus(UUID claimId, ClaimStatusUpdateCommand command, UUID adminId) {
        OrderClaim claim = orderClaimRepository.findById(claimId)
                .orElseThrow(() -> new BusinessException(OperationErrorCode.CLAIM_NOT_FOUND));

        ClaimStatus newStatus;
        try {
            newStatus = ClaimStatus.valueOf(command.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(OperationErrorCode.INVALID_CLAIM_STATUS);
        }

        // 상태가 PROCESSING으로 변경되는 경우에만 SAGA 로직 수행
        if (newStatus == ClaimStatus.PROCESSING && claim.getStatus() != ClaimStatus.PROCESSING) {
            try {
                // CircuitBreaker 수동 적용
                CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker");
                circuitBreaker.executeRunnable(() -> 
                    runClaimApprovalSaga(claim, adminId, newStatus, command.getRefundAmount())
                );
            } catch (CallNotPermittedException e) {
                log.error("[Saga] CircuitBreaker is OPEN. Blocking request for claimId: {}", claimId);
                throw e;
            } catch (Exception e) {
                log.error("[Saga] Approval Saga failed: {}", e.getMessage());
                // 보상 트랜잭션은 runClaimApprovalSaga 내부에서 호출되었을 것임
                throw new RuntimeException("클레임 승인 처리 중 장애가 발생했습니다: " + e.getMessage(), e);
            }
        } else {
            // 그 외의 경우 (예: REJECTED) 단순 상태 변경
            claimWriter.commitClaimStatus(claim, newStatus, command.getRefundAmount());
        }

        return ClaimDto.from(claim);
    }

    public void runClaimApprovalSaga(OrderClaim claim, UUID adminId, ClaimStatus newStatus, BigDecimal refundAmount) {
        Deque<Runnable> compensations = new ArrayDeque<>();
        try {
            log.info("[Saga] Starting Approval Saga for claim: {}", claim.getClaimId());

            // 1. Order Service에서 상위 orderId 및 상품 목록 조회
            OrderPort.CompanyOrderDetails details = orderPort.getCompanyOrderDetails(claim.getCompanyOrderId());

            // 2. Hub Service를 통해 재고 일괄 복원
            hubPort.returnStock(
                    details.orderId(),
                    details.items().stream()
                            .map(item -> new HubPort.InventoryItem(item.productOptionId(), item.quantity()))
                            .collect(Collectors.toList())
            );
            // 재고 복원 성공 시 보상 로직(재고 다시 차감) 등록
            compensations.push(() -> {
                log.info("[Saga-Comp] Rolling back stock for order: {}", details.orderId());
                hubPort.deductStock(
                    details.orderId(),
                    details.items().stream()
                            .map(item -> new HubPort.InventoryItem(item.productOptionId(), item.quantity()))
                            .collect(Collectors.toList())
                );
            });

            // 3. Order Service에서 주문 상태 변경 (CANCELLED)
            orderPort.cancelCompanyOrderByClaim(claim.getCompanyOrderId(), adminId);

            // 4. 모든 외부 작업 성공 후 로컬 DB 최종 반영 (Final TX)
            claimWriter.commitClaimStatus(claim, newStatus, refundAmount);
            log.info("[Saga] Successfully completed Approval Saga for claim: {}", claim.getClaimId());

        } catch (Exception e) {
            log.error("[Saga] Error occurred during Approval Saga: {}", e.getMessage());
            executeCompensations(compensations);
            throw e; 
        }
    }

    private void executeCompensations(Deque<Runnable> compensations) {
        log.info("[Saga] Starting compensations. Total steps: {}", compensations.size());
        while (!compensations.isEmpty()) {
            try {
                compensations.pop().run();
            } catch (Exception e) {
                log.error("[Saga] Compensation failed - Manual recovery needed: {}", e.getMessage(), e);
            }
        }
        log.info("[Saga] Compensations finished.");
    }
}
