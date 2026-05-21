package com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteCreateResponse {

    private UUID routeId;
    private UUID deliveryId;
    private Integer sequence;
    private UUID fromHubId;
    private UUID toHubId;
    private BigDecimal estimatedDistance;
    private LocalDateTime estimatedDuration;
    private BigDecimal actualDistance;
    private LocalDateTime actualDuration;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
}