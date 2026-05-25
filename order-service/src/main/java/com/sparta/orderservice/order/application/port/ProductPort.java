package com.sparta.orderservice.order.application.port;

import com.sparta.orderservice.order.application.dto.ProductOptionInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

// Product Service 상품 정보 조회 포트
// 구현체: order/infrastructure/client/ProductAdapter
public interface ProductPort {

    // 상품 옵션 ID 목록 → { productOptionId: (companyId, unitPrice) } 일괄 조회
    Map<UUID, ProductOptionInfo> getProductOptionInfos(List<UUID> productOptionIds);
}
