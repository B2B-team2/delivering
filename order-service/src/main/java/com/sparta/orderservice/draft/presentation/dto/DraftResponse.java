package com.sparta.orderservice.draft.presentation.dto;

import com.sparta.orderservice.draft.application.dto.DraftResult;

import java.util.UUID;

public record DraftResponse(
        UUID draftId,
        UUID userId,
        UUID productId,
        UUID productOptionId,
        int quantity
) {
    public static DraftResponse from(DraftResult result) {
        return new DraftResponse(
                result.draftId(),
                result.userId(),
                result.productId(),
                result.productOptionId(),
                result.quantity()
        );
    }
}
