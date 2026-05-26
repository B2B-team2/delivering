package com.sparta.orderservice.order.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Product Service에서 조회한 상품 옵션 정보 — application layer DTO
 * infrastructure layer의 ProductOptionInfoItem을 application 경계 안으로 변환하여 사용
 */
public record ProductOptionInfo(
        UUID productOptionId,
        UUID companyId,      // 공급업체 ID
        BigDecimal unitPrice
) {}