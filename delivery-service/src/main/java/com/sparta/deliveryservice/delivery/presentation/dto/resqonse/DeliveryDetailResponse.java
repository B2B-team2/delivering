package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
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
public class DeliveryDetailResponse {

    private UUID deliveryId;
    private UUID companyOrderId;
    private String trackingNumber;
    private String status;
    private String memo;
    private UUID departureHubId;
    private String departureHubName;
    private UUID destinationHubId;
    private String destinationHubName;
    private DeliveryAddress deliveryAddress;
    private String recipientName;
    private String recipientSlackId;
    private DeliveryManagerDto deliveryManager;
    private LocalDateTime finalDispatchDeadlineAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<DeliveryRouteDto> routes;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryManagerDto {
        private UUID deliveryManagerId;
        private String name;
        private String phone;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryRouteDto {
        private UUID routeId;
        private Integer sequence;
        private String fromHubName;
        private String toHubName;
        private BigDecimal estimatedDistance;
        private String estimatedDuration;
        private BigDecimal actualDistance;
        private String actualDuration;
        private String status;
    }
}