package com.sparta.orderservice.order.infrastructure.client.dto;

// Company Service: 수령업체 기본 배송지 조회 결과
public record DefaultDeliveryAddressResponse(
        String address,
        String addressDetail,
        String recipientName,
        String phone
) {}
