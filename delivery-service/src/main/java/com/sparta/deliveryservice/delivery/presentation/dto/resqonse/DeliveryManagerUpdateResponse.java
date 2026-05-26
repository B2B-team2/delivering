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
public class DeliveryManagerUpdateResponse {

    private UUID deliveryId;
    private PreviousManagerDto previousManager;
    private CurrentManagerDto currentManager;
    private UUID logId;
    private LocalDateTime updatedAt;
    private UUID updatedBy;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PreviousManagerDto {
        private UUID deliveryManagerId;
        private String name;
        private String phone;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CurrentManagerDto {
        private UUID deliveryManagerId;
        private String name;
        private String phone;
    }
}