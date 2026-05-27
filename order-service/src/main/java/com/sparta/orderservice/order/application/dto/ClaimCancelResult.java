package com.sparta.orderservice.order.application.dto;

import com.sparta.orderservice.order.domain.core.CompanyOrder;

import java.util.UUID;

/**
 * 클레임 취소 결과 (내부 API 전용)
 */
public record ClaimCancelResult(
        UUID companyOrderId,
        UUID orderId,
        String companyOrderStatus,
        String orderStatus
) {
    public static ClaimCancelResult from(CompanyOrder companyOrder) {
        return new ClaimCancelResult(
                companyOrder.getCompanyOrderId(),
                companyOrder.getOrder().getOrderId(),
                companyOrder.getStatus().name(),
                companyOrder.getOrder().getStatus().name()
        );
    }
}
