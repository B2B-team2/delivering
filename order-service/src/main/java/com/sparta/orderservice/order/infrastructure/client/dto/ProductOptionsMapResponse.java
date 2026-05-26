package com.sparta.orderservice.order.infrastructure.client.dto;

import java.util.Map;

// Product Service: 상품 옵션 일괄 조회 응답
// { "optionsMap": { "productOptionId(String)": { productOptionId, companyId, unitPrice }, ... } }
public record ProductOptionsMapResponse(
        Map<String, ProductOptionInfoItem> optionsMap
) {}
