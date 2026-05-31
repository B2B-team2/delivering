package com.sparta.orderservice.order.presentation.dto;

/**
 * 업체 주문 수령 완료 요청 (내부 API 전용)
 * delivery-service가 전달하는 body — order-service 상태는 DB 기준으로 직접 판단
 */
public record CompanyOrderDeliveredRequest(
        String companyOrderStatus, // DELIVERED
        String orderStatus         // COMPLETED or DELIVERING (delivery-service 관점, 참고용)
) {
}
