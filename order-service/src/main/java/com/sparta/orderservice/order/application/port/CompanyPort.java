package com.sparta.orderservice.order.application.port;

import com.sparta.orderservice.order.infrastructure.client.dto.DefaultDeliveryAddressResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Company Service 업체 정보 조회 포트
// 구현체: order/infrastructure/client/CompanyAdapter
public interface CompanyPort {

    // 업체 ID 목록 → { companyId: hubId } 매핑 일괄 조회
    Map<UUID, UUID> getHubIds(List<UUID> companyIds);

    // 수령업체의 기본 배송지(is_default=true) 조회 (draft → order 전환 시 배송지 자동 세팅)
    DefaultDeliveryAddressResponse getDefaultDeliveryAddress(UUID receiverCompanyId);
}
