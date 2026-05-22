package com.sparta.orderservice.draft.application.dto;

import com.sparta.orderservice.draft.domain.core.Draft;

import java.util.UUID;

public record DraftResult(
        UUID draftId,
        UUID userId,
        UUID productId,
        UUID productOptionId,
        int quantity
) {
    public static DraftResult from(Draft draft) {
        return new DraftResult(
                draft.getDraftId(),
                draft.getUserId(),
                draft.getProductId(),
                draft.getProductOptionId(),
                draft.getQuantity()
        );
    }
}
