package com.sparta.orderservice.order.domain.core;

public enum OrderStatus {
    PENDING,     // 준비중
    DELIVERING,  // 배송중
    COMPLETED,   // 배송완료
    CANCELLED    // 주문취소
}
