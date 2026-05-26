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
public class ProductOptionDeleteResponse {
    private UUID productOptionId;
    private LocalDateTime deletedAt;

    public static ProductOptionDeleteResponse of(UUID productOptionId, LocalDateTime deletedAt) {
        return ProductOptionDeleteResponse.builder()
                .productOptionId(productOptionId)
                .deletedAt(deletedAt)
                .build();
    }
}
