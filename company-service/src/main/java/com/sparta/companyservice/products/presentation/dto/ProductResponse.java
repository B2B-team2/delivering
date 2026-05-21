package com.sparta.companyservice.products.presentation.dto;

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
public class ProductResponse {
    private UUID productId;
    private UUID companyId;
    private UUID categoryId;
    private String name;
    private BigDecimal price;
    private String description;
    private String thumbnailUrl;
    private ProductStatusEnum status;
}
