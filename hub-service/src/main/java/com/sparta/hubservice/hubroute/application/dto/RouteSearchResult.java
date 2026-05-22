package com.sparta.hubservice.hubroute.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RouteSearchResult {

    private final UUID fromHubId;
    private final UUID toHubId;
    private final int totalDuration;
    private final BigDecimal totalDistance;
    private final List<Segment> routes;

    @Getter
    @Builder
    public static class Segment {
        private final int sequence;
        private final UUID routeId;
        private final UUID fromHubId;
        private final String fromHubName;
        private final UUID toHubId;
        private final String toHubName;
        private final int duration;
        private final BigDecimal distance;
    }
}
