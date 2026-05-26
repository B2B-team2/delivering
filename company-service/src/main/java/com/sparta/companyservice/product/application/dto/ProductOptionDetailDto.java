package com.sparta.companyservice.product.application.dto;

import com.sparta.companyservice.product.domain.core.ProductOption;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductOptionDetailDto {
    private UUID productOptionId;
    private UUID companyId;
    private BigDecimal unitPrice;

    public static ProductOptionDetailDto from(ProductOption entity) {
        return ProductOptionDetailDto.builder()
                .productOptionId(entity.getProductOptionId())
                .companyId(entity.getProduct().getCompanyId())
                .unitPrice(entity.getProduct().getPrice().add(entity.getExtraPrice()))
                .build();
    }
}
