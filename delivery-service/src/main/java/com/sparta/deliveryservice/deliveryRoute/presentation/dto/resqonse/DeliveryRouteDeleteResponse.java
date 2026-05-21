package com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse;

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
public class DeliveryRouteDeleteResponse {

    private UUID deliveryId;
    private UUID deletedRouteId;
    private List<RemainingRouteDto> remainingRoutes;
    private UUID logId;
    private LocalDateTime deletedAt;
    private String deletedBy;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class RemainingRouteDto {
        private UUID routeId;
        private Integer sequence;
        private UUID fromHubId;
        private UUID toHubId;
        private String status;
    }
}