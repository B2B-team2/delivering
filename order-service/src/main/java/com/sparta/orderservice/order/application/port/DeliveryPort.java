package com.sparta.orderservice.order.application.port;

import com.sparta.orderservice.order.domain.core.Order;

import java.util.Map;
import java.util.UUID;

// Delivery Service 배송 생성 포트
// 구현체: order/infrastructure/client/DeliveryAdapter
public interface DeliveryPort {

    // 배송 일괄 생성: CompanyOrder 전체를 한 번에 요청
    // hubIdMap: { companyId → hubId } 전체 매핑 - Adapter에서 공급업체 companyId로 departureHubId 조회
    // destinationHubId: 수령업체 소속 허브 (도착 허브, 공통)
    // 리턴: Map<companyOrderId, deliveryId>
    Map<UUID, UUID> createDeliveries(Order order, Map<UUID, UUID> hubIdMap, UUID destinationHubId);
}
