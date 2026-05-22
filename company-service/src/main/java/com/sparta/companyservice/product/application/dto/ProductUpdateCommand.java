package com.sparta.companyservice.product.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class ProductUpdateCommand {
    private UUID companyId;
    private UUID categoryId;
    private String name;
    private BigDecimal price;
    private String description;
    private String thumbnailUrl;
    private String status;
}
