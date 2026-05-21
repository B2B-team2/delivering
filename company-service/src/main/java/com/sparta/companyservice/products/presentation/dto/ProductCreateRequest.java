package com.sparta.companyservice.products.presentation.dto;

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
public class ProductCreateRequest {
    private UUID companyId;
    private UUID categoryId;
    private String name;
    private BigDecimal price;
    private String description;
    private String thumbnailUrl;
}
