package com.sparta.deliveryservice.deliveryRoute.domain.core;

public enum RouteStatus {
    PENDING,      // 이동 대기
    IN_TRANSIT,   // 이동 중
    ARRIVED,      // 해당 허브 도착 완료
    SKIP,          // 우회 또는 건너뜀
    CANCELLED      // 취소됌
}