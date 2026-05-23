package com.sparta.hubservice.hubroute.presentation.dto;

import com.sparta.hubservice.hubroute.application.dto.RouteSearchResult;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class RouteSearchResponse {

    private final UUID fromHubId;
    private final UUID toHubId;
    private final BigDecimal totalDistance;
    private final String totalDuration;
    private final List<RouteStep> routes;

    public static RouteSearchResponse from(RouteSearchResult result) {
        List<RouteStep> steps = result.getRoutes().stream()
                .map(RouteStep::from)
                .toList();
        return RouteSearchResponse.builder()
                .fromHubId(result.getFromHubId())
                .toHubId(result.getToHubId())
                .totalDistance(result.getTotalDistance())
                .totalDuration(formatDuration(result.getTotalDuration()))
                .routes(steps)
                .build();
    }

    private static String formatDuration(int minutes) {
        return String.format("%02d:%02d:00", minutes / 60, minutes % 60);
    }

    @Getter
    @Builder
    public static class RouteStep {
        private final int sequence;
        private final UUID routeId;
        private final UUID fromHubId;
        private final String fromHubName;
        private final UUID toHubId;
        private final String toHubName;
        private final String duration;
        private final BigDecimal distance;

        public static RouteStep from(RouteSearchResult.Segment segment) {
            return RouteStep.builder()
                    .sequence(segment.getSequence())
                    .routeId(segment.getRouteId())
                    .fromHubId(segment.getFromHubId())
                    .fromHubName(segment.getFromHubName())
                    .toHubId(segment.getToHubId())
                    .toHubName(segment.getToHubName())
                    .duration(formatDuration(segment.getDuration()))
                    .distance(segment.getDistance())
                    .build();
        }

        private static String formatDuration(int minutes) {
            return String.format("%02d:%02d:00", minutes / 60, minutes % 60);
        }
    }
}
