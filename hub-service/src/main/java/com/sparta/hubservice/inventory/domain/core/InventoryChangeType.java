package com.sparta.hubservice.inventory.domain.core;

public enum InventoryChangeType {
    INBOUND,      // 입고
    OUTBOUND,     // 출고
    RESERVED,     // 예약
    CANCELLED,    // 예약 취소
    ADJUSTED,     // 재고 조정
    RETURNED      // 반품 입고
}
