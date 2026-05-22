package com.sparta.companyservice.product.presentation.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDeleteResponse(
        UUID productId,
        LocalDateTime deletedAt
) {
    public static ProductDeleteResponse from(UUID productId, LocalDateTime deletedAt) {
        return new ProductDeleteResponse(productId, deletedAt);
    }
}
