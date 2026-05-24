package com.sparta.operationsservice.claim.presentation.dto;

import com.sparta.operationsservice.claim.application.dto.ClaimCreateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ClaimCreateRequest {

    @NotNull(message = "주문 상품 ID는 필수입니다.")
    private UUID orderItemId;

    @NotBlank(message = "클레임 타입은 필수입니다.")
    private String claimType;

    @NotBlank(message = "사유는 필수입니다.")
    private String reason;

    private BigDecimal refundAmount;

    public ClaimCreateCommand toCommand() {
        return ClaimCreateCommand.builder()
                .orderItemId(this.orderItemId)
                .claimType(this.claimType)
                .reason(this.reason)
                .refundAmount(this.refundAmount != null ? this.refundAmount : BigDecimal.ZERO)
                .build();
    }
}
