package com.sparta.orderservice.draft.presentation.dto;

import jakarta.validation.constraints.Positive;

public record DraftUpdateRequest(
        @Positive int quantity
) {}
