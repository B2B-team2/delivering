package com.sparta.operationsservice.claim.presentation.dto;

import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ClaimResponse {
    private UUID claimId;
    private UUID orderItemId;
    private String claimType;
    private String status;
    private String reason;
    private BigDecimal refundAmount;

    public static ClaimResponse from(ClaimDto dto) {
        return ClaimResponse.builder()
                .claimId(dto.getClaimId())
                .orderItemId(dto.getOrderItemId())
                .claimType(dto.getClaimType())
                .status(dto.getStatus())
                .reason(dto.getReason())
                .refundAmount(dto.getRefundAmount())
                .build();
    }
}
