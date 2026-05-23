package com.sparta.companyservice.product.application.dto;

import com.sparta.companyservice.product.domain.core.ProductCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CategoryDto {
    private UUID categoryId;
    private String name;
    private Integer depth;

    public static CategoryDto from(ProductCategory entity) {
        return CategoryDto.builder()
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .depth(entity.getDepth())
                .build();
    }
}
