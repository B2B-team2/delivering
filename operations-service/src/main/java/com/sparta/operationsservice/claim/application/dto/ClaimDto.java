package com.sparta.operationsservice.claim.application.dto;

import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClaimDto {
    private UUID claimId;
    private UUID orderItemId;
    private String claimType;
    private String status;
    private String reason;
    private BigDecimal refundAmount;
    private LocalDateTime deletedAt;

    public static ClaimDto from(OrderClaim entity) {
        return ClaimDto.builder()
                .claimId(entity.getClaimId())
                .orderItemId(entity.getOrderItemId())
                .claimType(entity.getClaimType().name())
                .status(entity.getStatus().name())
                .reason(entity.getReason())
                .refundAmount(entity.getRefundAmount())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
