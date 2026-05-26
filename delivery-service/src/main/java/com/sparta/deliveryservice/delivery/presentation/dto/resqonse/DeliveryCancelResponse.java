package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryCancelResponse {

    private UUID deliveryId;
    private String trackingNumber;
    private String previousStatus;
    private String currentStatus;
    private List<CancelledRouteDto> cancelledRoutes;
    private UUID logId;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CancelledRouteDto {
        private UUID routeId;
        private Integer sequence;
        private String status;
    }
}