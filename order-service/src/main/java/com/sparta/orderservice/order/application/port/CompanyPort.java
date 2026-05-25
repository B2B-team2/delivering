package com.sparta.orderservice.order.application.port;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Company Service 업체 정보 조회 포트
// 구현체: order/infrastructure/client/CompanyAdapter
public interface CompanyPort {

    // 업체 ID 목록 → { companyId: hubId } 매핑 일괄 조회
    Map<UUID, UUID> getHubIds(List<UUID> companyIds);
}
