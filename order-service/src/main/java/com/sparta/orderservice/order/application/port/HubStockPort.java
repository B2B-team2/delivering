package com.sparta.orderservice.order.application.port;

import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;

import java.util.UUID;

// Hub Service 재고 관련 외부 호출 포트
// 구현체: order/infrastructure/client/HubStockAdapter
public interface HubStockPort {

    // 재고 예약: 주문 생성 시 호출, CompanyOrder별 반복 호출은 Adapter에서 처리
    // 실패 시 예외 전파 → 주문 생성 전체 롤백
    void reserveStock(Order order);

    // 재고 예약 전체 취소: 주문 전체 취소 시 호출 (orderId 기준)
    void cancelStock(UUID orderId);

    // 재고 예약 부분 취소: CompanyOrder 단위 취소 시 호출 (companyOrderId 기준)
    void cancelCompanyStock(UUID companyOrderId);

    // 재고 차감: 출고(SHIPPED) 처리 시 호출
    void deductStock(CompanyOrder companyOrder);

    // 재고 단건 재예약: cancelCompanyOrder Saga 보상 전용
    // cancelCompanyStock 성공 후 이후 단계 실패 시 해당 CompanyOrder의 재고를 복원
    void reserveCompanyStock(CompanyOrder companyOrder);
}
