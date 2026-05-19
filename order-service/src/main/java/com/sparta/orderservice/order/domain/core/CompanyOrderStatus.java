package com.sparta.orderservice.order.domain.core;

public enum CompanyOrderStatus {
    ORDERED,     // 주문접수
    PREPARING,   // 출고준비중
    SHIPPED,     // 상품출고
    DELIVERED,   // 수령업체도착
    CANCELLED    // 주문취소
}
