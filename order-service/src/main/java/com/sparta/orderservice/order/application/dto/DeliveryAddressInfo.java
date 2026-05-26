package com.sparta.orderservice.order.application.dto;

/**
 * Company Service에서 조회한 기본 배송지 정보 — application layer DTO
 * infrastructure layer의 DefaultDeliveryAddressResponse를 application 경계 안으로 변환하여 사용
 * address: p_orders.address(jsonb) 저장 형식으로 미리 변환된 값 — {"address": "...", "address_detail": "..."}
 * addressDetail은 CompanyAdapter에서 JSON 조립 시 소비되므로 application layer에 노출하지 않음
 */
public record DeliveryAddressInfo(
        String address,      // jsonb 형식으로 변환 완료된 주소 문자열
        String recipientName,
        String phone
) {}
