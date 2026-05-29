package com.sparta.operationsservice.ai.presentation.dto.response;

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
public class AiResponse {
    private UUID requestId;
    private UUID userId;
    private UUID deliveryId;
    private String aiModelName;
    private String promptText;
    private String responseText;
    private String status;
    private String errorMessage;
    private LocalDateTime finalDeadlineAt;
}