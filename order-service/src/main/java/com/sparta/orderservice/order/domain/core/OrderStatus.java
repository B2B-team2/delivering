package com.sparta.orderservice.order.domain.core;

public enum OrderStatus {
    PENDING,     // 준비중 - 주문접수. 출고 전 (CompanyOrder 전체가 ORDERED/PREPARING)
    DELIVERING,  // 배송중 (하나 이상의 CompanyOrder가 SHIPPED)
    COMPLETED,   // 배송완료 (모든 CompanyOrder가 DELIVERED)
    CANCELLED    // 주문취소
}
