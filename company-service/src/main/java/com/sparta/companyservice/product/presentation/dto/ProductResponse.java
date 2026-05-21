package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductDto;
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
public class ProductResponse {
    private UUID productId;
    private UUID companyId;
    private UUID categoryId;
    private String name;
    private BigDecimal price;
    private String description;
    private String thumbnailUrl;
    private String status;

    public static ProductResponse from(ProductDto dto) {
        return ProductResponse.builder()
                .productId(dto.getProductId())
                .companyId(dto.getCompanyId())
                .categoryId(dto.getCategoryId())
                .name(dto.getName())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .thumbnailUrl(dto.getThumbnailUrl())
                .status(dto.getStatus())
                .build();
    }
}
