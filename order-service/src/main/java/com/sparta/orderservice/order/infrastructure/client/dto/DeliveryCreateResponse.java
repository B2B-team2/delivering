package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.UUID;

// Delivery Service 배송 생성 응답 DTO
public record DeliveryCreateResponse(
        UUID deliveryId,
        UUID companyOrderId,
        String status
) {
}
