package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryTrackingResponse {

    private UUID deliveryId;
    private String trackingNumber;
    private String status;
    private String departureHubName;
    private String destinationHubName;
    private DeliveryAddress deliveryAddress;
    private String recipientName;
    private String deliveryManagerName;
    private LocalDateTime finalDispatchDeadlineAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private CurrentLocationDto currentLocation;
    private List<DeliveryRouteOverviewDto> routes;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CurrentLocationDto {
        private Integer sequence;
        private String fromHubName;
        private String toHubName;
        private String status;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryRouteOverviewDto {
        private Integer sequence;
        private String fromHubName;
        private String toHubName;
        private String status;
    }
}