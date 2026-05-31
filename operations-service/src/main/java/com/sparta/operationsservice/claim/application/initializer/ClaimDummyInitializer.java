package com.sparta.operationsservice.claim.application.initializer;

import com.sparta.operationsservice.claim.domain.core.ClaimStatus;
import com.sparta.operationsservice.claim.domain.core.ClaimType;
import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimDummyInitializer implements ApplicationRunner {

    private final OrderClaimRepository orderClaimRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (orderClaimRepository.count() == 0) {
            log.info("🎯 [OperationsService] 로컬 개발용 초기 클레임 데이터 주입 시작");

            saveClaim(UUID.randomUUID(), ClaimType.RETURN, "상품 파손으로 인한 반품 요청", BigDecimal.valueOf(50000));
            saveClaim(UUID.randomUUID(), ClaimType.EXCHANGE, "사이즈 오배송으로 인한 교환 요청", BigDecimal.ZERO);
            saveClaim(UUID.randomUUID(), ClaimType.RETURN, "단순 변심 반품", BigDecimal.valueOf(15000));

            log.info("🎯 [OperationsService] 초기 클레임 데이터 주입 완료 (3개)");
        }
    }

    private void saveClaim(UUID companyOrderId, ClaimType type, String reason, BigDecimal refundAmount) {
        OrderClaim claim = OrderClaim.builder()
                .companyOrderId(companyOrderId)
                .claimType(type)
                .reason(reason)
                .refundAmount(refundAmount)
                .status(ClaimStatus.REQUESTED)
                .build();

        orderClaimRepository.save(claim);
    }
}
