package com.sparta.operationsservice.ai.presentation.dto.requset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiRequest {
    private UUID userId;
    private UUID deliveryId;
    private String fromHubName;
    private String deliveryAddress;
    private String aiModelName;
    private String promptText;
}