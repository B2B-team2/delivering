package com.sparta.orderservice.draft.application.dto;

import java.util.UUID;

public record AddDraftCommand(
        UUID userId,
        UUID productId,
        UUID productOptionId,
        int quantity
) {}
