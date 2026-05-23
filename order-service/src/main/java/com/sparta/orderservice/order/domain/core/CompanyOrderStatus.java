package com.sparta.orderservice.order.domain.core;

public enum CompanyOrderStatus {
    ORDERED,     // 주문접수
    PREPARING,   // 출고준비중
    SHIPPED,     // 상품출고 (상품 출고 완료, 수령업체로 이동 중)
    DELIVERED,   // 수령업체도착 (수령업체 도착 확인)
    CANCELLED    // 주문취소
}
