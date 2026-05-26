package com.sparta.orderservice.order.infrastructure.client.dto;

import java.math.BigDecimal;
import java.util.UUID;

// Product Service: 상품 옵션 단건 조회 결과
public record ProductOptionInfoItem(
        UUID productOptionId,
        UUID companyId,     // 공급업체 ID
        BigDecimal unitPrice
) {}
