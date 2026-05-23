package com.sparta.companyservice.product.application.dto;

import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductOptionDto {
    private UUID productOptionId;
    private UUID productId;
    private String optionsName;
    private BigDecimal extraPrice;
    private ProductStatusEnum status;
    private Integer displayOrder;
    private LocalDateTime deletedAt;

    public static ProductOptionDto from(ProductOption entity) {
        return ProductOptionDto.builder()
                .productOptionId(entity.getProductOptionId())
                .productId(entity.getProduct().getProductId())
                .optionsName(entity.getOptionsName())
                .extraPrice(entity.getExtraPrice())
                .status(entity.getStatus())
                .displayOrder(entity.getDisplayOrder())
                .deletedAt(entity.getDeletedAt())
                .build();
    }
}
