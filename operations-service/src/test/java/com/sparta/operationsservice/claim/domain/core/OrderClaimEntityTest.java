package com.sparta.operationsservice.claim.domain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderClaimEntityTest {

    @Test
    @DisplayName("엔티티 생성 시 기본값 검증: status는 REQUESTED, refundAmount는 0이어야 하는가?")
    void entityCreationDefaultValueTest() {
        // given
        UUID orderItemId = UUID.randomUUID();
        String reason = "Damaged product";

        // when
        OrderClaim claim = OrderClaim.builder()
                .orderItemId(orderItemId)
                .claimType(ClaimType.RETURN)
                .reason(reason)
                .build();

        // then
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.REQUESTED);
        assertThat(claim.getRefundAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(claim.getReason()).isEqualTo(reason);
    }

    @Test
    @DisplayName("상태 업데이트 검증: updateStatus 호출 시 status 필드가 변경되는가?")
    void updateStatusTest() {
        // given
        OrderClaim claim = OrderClaim.builder()
                .status(ClaimStatus.REQUESTED)
                .build();

        // when
        claim.updateStatus(ClaimStatus.PROCESSING);

        // then
        assertThat(claim.getStatus()).isEqualTo(ClaimStatus.PROCESSING);
    }
}
