package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Time;
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
    private UUID companyReceiveId;
    private String trackingNumber;
    private String status;
    private UUID departureHubId;
    private String departureHubName;
    private UUID destinationHubId;
    private String destinationHubName;
    private DeliveryAddress deliveryAddress;
    private String recipientName;
    private String phone;
    private String postalCode;
    private String recipientSlackId;
    private UUID deliveryManagerId;
    private String deliveryManagerName;
    private String deliveryManagerPhone;
    private String memo;
    private LocalDateTime finalDispatchDeadlineAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<DeliveryRouteResponseDto> routes;

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
        private Time estimatedDuration;
        private String status;
    }
}