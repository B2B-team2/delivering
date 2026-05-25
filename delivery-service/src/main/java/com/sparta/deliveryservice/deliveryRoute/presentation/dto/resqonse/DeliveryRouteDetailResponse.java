package com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteDetailResponse {

    private UUID deliveryId;
    private BigDecimal totalEstimatedDistance;
    private String totalEstimatedDuration;
    private BigDecimal totalActualDistance;
    private String totalActualDuration;
    private List<DeliveryRouteDetailDto> routes;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryRouteDetailDto {
        private UUID routeId;
        private Integer sequence;
        private UUID fromHubId;
        private String fromHubName;
        private UUID toHubId;
        private String toHubName;
        private BigDecimal estimatedDistance;
        private String estimatedDuration;
        private BigDecimal actualDistance;
        private String actualDuration;
        private String status;
    }
}