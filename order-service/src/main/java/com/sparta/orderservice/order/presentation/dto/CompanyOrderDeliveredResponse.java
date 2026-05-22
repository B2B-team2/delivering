package com.sparta.orderservice.order.presentation.dto;

import com.sparta.orderservice.order.application.dto.CompanyOrderDeliveredResult;

import java.util.UUID;

public record CompanyOrderDeliveredResponse(
        UUID companyOrderId,
        UUID orderId,
        String companyOrderStatus,  // DELIVERED
        String orderStatus          // COMPLETED or DELIVERING
) {
    public static CompanyOrderDeliveredResponse from(CompanyOrderDeliveredResult result) {
        return new CompanyOrderDeliveredResponse(
                result.companyOrderId(),
                result.orderId(),
                result.companyOrderStatus(),
                result.orderStatus()
        );
    }
}
