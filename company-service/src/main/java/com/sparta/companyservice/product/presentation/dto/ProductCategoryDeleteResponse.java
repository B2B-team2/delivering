package com.sparta.companyservice.product.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDeleteResponse {
    private UUID categoryId;
    private LocalDateTime deletedAt;

    public static ProductCategoryDeleteResponse of(UUID categoryId, LocalDateTime deletedAt) {
        return ProductCategoryDeleteResponse.builder()
                .categoryId(categoryId)
                .deletedAt(deletedAt)
                .build();
    }
}
