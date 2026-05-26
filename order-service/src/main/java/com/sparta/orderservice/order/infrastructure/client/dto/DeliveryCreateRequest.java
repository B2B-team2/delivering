package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.UUID;

// Delivery Service 배송 생성 요청 DTO
public record DeliveryCreateRequest(
        UUID companyOrderId,       // 서브 주문 ID
        UUID departureHubId,       // 출발 허브 ID (공급업체 소속 허브)
        UUID destinationHubId,     // 도착 허브 ID (수령업체 소속 허브)
        String deliveryAddress,    // 최종 배송지 주소 (JSON 문자열 형태: {"address": "기본주소", "address_detail": "상세주소"}) - Delivery Service에서 파싱 처리
        String recipientName,      // 수령인 이름
        String recipientSlackId,   // 수령인 Slack ID
        String memo                // 요청 사항 (order.requestMemo 사용 - 별도 배송 메모 필요 시 Delivery Service 담당자 확인 필요)
) {
}
