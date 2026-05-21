package com.sparta.companyservice.products.application.dto;

import com.sparta.companyservice.products.domain.core.Product;
import com.sparta.companyservice.products.domain.core.ProductStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private UUID productId;
    private UUID companyId;
    private UUID categoryId;
    private String name;
    private BigDecimal price;
    private String description;
    private String thumbnailUrl;
    private ProductStatusEnum status;

    public static ProductDto from(Product entity) {
        return ProductDto.builder()
                .productId(entity.getProductId())
                .companyId(entity.getCompanyId())
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .price(entity.getPrice())
                .description(entity.getDescription())
                .thumbnailUrl(entity.getThumbnailUrl())
                .status(entity.getStatus())
                .build();
    }
}
