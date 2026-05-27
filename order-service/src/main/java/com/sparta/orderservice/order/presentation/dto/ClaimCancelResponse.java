package com.sparta.orderservice.order.presentation.dto;

import com.sparta.orderservice.order.application.dto.ClaimCancelResult;

import java.util.UUID;

/**
 * 클레임 취소 응답 (내부 API 전용)
 */
public record ClaimCancelResponse(
        UUID companyOrderId,
        UUID orderId,
        String companyOrderStatus,
        String orderStatus
) {
    public static ClaimCancelResponse from(ClaimCancelResult result) {
        return new ClaimCancelResponse(
                result.companyOrderId(),
                result.orderId(),
                result.companyOrderStatus(),
                result.orderStatus()
        );
    }
}
