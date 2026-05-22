package com.sparta.orderservice.draft.presentation.dto;

import com.sparta.orderservice.draft.application.dto.AddDraftCommand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record DraftAddRequest(
        @NotNull UUID productId,
        @NotNull UUID productOptionId,
        @Positive int quantity
) {
    public AddDraftCommand toCommand(UUID userId) {
        return new AddDraftCommand(userId, productId, productOptionId, quantity);
    }
}
