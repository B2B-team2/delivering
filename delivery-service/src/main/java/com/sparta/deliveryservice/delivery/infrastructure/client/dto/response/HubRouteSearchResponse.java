package com.sparta.deliveryservice.delivery.infrastructure.client.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HubRouteSearchResponse {

    private UUID fromHubId;
    private UUID toHubId;
    private BigDecimal totalDistance;
    private Time totalDuration;
    private List<HubRouteDto> routes;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class HubRouteDto {
        private Integer sequence;
        private UUID routeId;
        private UUID fromHubId;
        private String fromHubName;
        private UUID toHubId;
        private String toHubName;
        private Time duration;
        private BigDecimal distance;
    }
}