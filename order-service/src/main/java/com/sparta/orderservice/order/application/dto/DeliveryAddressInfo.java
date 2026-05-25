package com.sparta.orderservice.order.application.dto;

/**
 * Company Service에서 조회한 기본 배송지 정보 — application layer DTO
 * infrastructure layer의 DefaultDeliveryAddressResponse를 application 경계 안으로 변환하여 사용
 */
public record DeliveryAddressInfo(
        String address,
        String addressDetail,
        String recipientName,
        String phone
) {}
