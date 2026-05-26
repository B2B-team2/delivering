package com.sparta.hubservice.hubroute.application.dto;

import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import com.sparta.hubservice.hubroute.domain.port.HubInfo;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class HubRouteDto {

    private final UUID routeId;
    private final HubInfoDto fromHub;
    private final HubInfoDto toHub;
    private final String duration;
    private final BigDecimal distance;
    private final LocalDateTime createdAt;
    private final UUID createdBy;
    private final LocalDateTime updatedAt;
    private final UUID updatedBy;

    public static HubRouteDto from(HubRoute route) {
        return from(route, null, null);
    }

    public static HubRouteDto from(HubRoute route, HubInfo fromHubInfo, HubInfo toHubInfo) {
        return HubRouteDto.builder()
                .routeId(route.getRouteId())
                .fromHub(HubInfoDto.from(fromHubInfo))
                .toHub(HubInfoDto.from(toHubInfo))
                .duration(formatDuration(route.getDuration()))
                .distance(route.getDistance())
                .createdAt(route.getCreatedAt())
                .createdBy(route.getCreatedBy())
                .updatedAt(route.getUpdatedAt())
                .updatedBy(route.getUpdatedBy())
                .build();
    }

    private static String formatDuration(int minutes) {
        return String.format("%02d:%02d:%02d", minutes / 60, minutes % 60, 0);
    }

    @Getter
    @Builder
    public static class HubInfoDto {
        private final UUID hubId;
        private final String name;
        private final String address;

        public static HubInfoDto from(HubInfo info) {
            if (info == null) return null;
            return HubInfoDto.builder()
                    .hubId(info.hubId())
                    .name(info.name())
                    .address(info.address())
                    .build();
        }
    }
}
