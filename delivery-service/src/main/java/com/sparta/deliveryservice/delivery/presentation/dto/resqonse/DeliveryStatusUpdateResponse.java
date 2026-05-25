package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryStatusUpdateResponse {

    private UUID deliveryId;
    private String previousStatus;
    private String currentStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private UUID logId;
    private LocalDateTime updatedAt;
    private UUID updatedBy;
}