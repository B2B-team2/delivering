package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryCreateResponse {

    private UUID deliveryId;
    private UUID companyOrderId;
    private String trackingNumber;
    private String status;
    private UUID departureHubId;
    private String departureHubName;
    private UUID destinationHubId;
    private String destinationHubName;
    private String deliveryAddress;
    private String recipientName;
    private String recipientSlackId;
    private UUID deliveryManagerId;
    private String memo;
    private LocalDateTime finalDispatchDeadlineAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<DeliveryRouteResponseDto> routes;
    private LocalDateTime createdAt;
    private String createdBy;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryRouteResponseDto {
        private UUID routeId;
        private Integer sequence;
        private UUID fromHubId;
        private UUID toHubId;
        private BigDecimal estimatedDistance;
        private String estimatedDuration;
        private String status;
    }
}