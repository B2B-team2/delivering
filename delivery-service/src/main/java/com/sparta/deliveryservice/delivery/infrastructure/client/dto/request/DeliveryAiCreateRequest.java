package com.sparta.deliveryservice.delivery.infrastructure.client.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class DeliveryAiCreateRequest {
    private UUID userId;
    private UUID deliveryId;
    private String fromHubName;
    private String deliveryAddress;
    private String aiModelName;
    private String promptText;
}