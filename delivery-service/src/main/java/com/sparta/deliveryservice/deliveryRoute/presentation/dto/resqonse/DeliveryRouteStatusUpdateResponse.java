package com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteStatusUpdateResponse {

    private UUID routeId;
    private UUID deliveryId;
    private Integer sequence;
    private String previousStatus;
    private String currentStatus;
    private BigDecimal estimatedDistance;
    private String estimatedDuration;
    private BigDecimal actualDistance;
    private String actualDuration;
    private UUID logId;

}