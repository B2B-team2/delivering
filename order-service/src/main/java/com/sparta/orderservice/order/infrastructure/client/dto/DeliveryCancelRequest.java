package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.UUID;

// Saga 보상 전용: 배송 취소 단건 요청 DTO
// delivery-service 수신 형식: List<DeliveryCancelRequest> (배열)
// ※ delivery-service DTO의 orderId => 실제 companyOrderId
public record DeliveryCancelRequest(UUID companyOrderId) {
}
