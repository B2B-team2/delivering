package com.sparta.orderservice.order.infrastructure.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOptionDetailsResponse {
    private Map<UUID, ProductOptionDetail> optionsMap;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductOptionDetail {
        private UUID productOptionId;
        private UUID companyId;
        private BigDecimal unitPrice;
    }
}
