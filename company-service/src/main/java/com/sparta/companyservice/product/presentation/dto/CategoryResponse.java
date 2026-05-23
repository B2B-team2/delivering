package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.CategoryDto;
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
public class CategoryResponse {
    private UUID categoryId;
    private String name;
    private Integer depth;

    public static CategoryResponse from(CategoryDto dto) {
        return CategoryResponse.builder()
                .categoryId(dto.getCategoryId())
                .name(dto.getName())
                .depth(dto.getDepth())
                .build();
    }
}
