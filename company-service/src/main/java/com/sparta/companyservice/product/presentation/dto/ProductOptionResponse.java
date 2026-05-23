package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductOptionDto;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
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
public class ProductOptionResponse {
    private UUID productOptionId;
    private UUID productId;
    private String optionsName;
    private BigDecimal extraPrice;
    private ProductStatusEnum status;
    private Integer displayOrder;

    public static ProductOptionResponse from(ProductOptionDto dto) {
        return ProductOptionResponse.builder()
                .productOptionId(dto.getProductOptionId())
                .productId(dto.getProductId())
                .optionsName(dto.getOptionsName())
                .extraPrice(dto.getExtraPrice())
                .status(dto.getStatus())
                .displayOrder(dto.getDisplayOrder())
                .build();
    }
}
