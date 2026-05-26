package com.sparta.operationsservice.claim.application.dto;

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
public class ClaimCreateCommand {
    private UUID orderItemId;
    private String claimType;
    private String reason;
    private BigDecimal refundAmount;
}
