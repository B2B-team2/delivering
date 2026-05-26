package com.sparta.orderservice.order.application.dto;

import com.sparta.orderservice.order.domain.core.CompanyOrder;

import java.util.UUID;

// 업체 주문 수령 완료 처리 결과 (내부 API 전용)
public record CompanyOrderDeliveredResult(
        UUID companyOrderId,
        UUID orderId,
        String companyOrderStatus,  // DELIVERED
        String orderStatus          // COMPLETED(모든 업체 주문 수령 완료) or DELIVERING(일부 배송 중)
) {
    public static CompanyOrderDeliveredResult from(CompanyOrder companyOrder) {
        return new CompanyOrderDeliveredResult(
                companyOrder.getCompanyOrderId(),
                companyOrder.getOrder().getOrderId(),
                companyOrder.getStatus().name(),
                companyOrder.getOrder().getStatus().name()
        );
    }
}
