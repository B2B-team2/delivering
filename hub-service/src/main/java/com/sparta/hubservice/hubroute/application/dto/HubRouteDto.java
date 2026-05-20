package com.sparta.hubservice.hubroute.application.dto;

import com.sparta.hubservice.hubroute.domain.core.HubRoute;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class HubRouteDto {

    private final UUID routeId;
    private final UUID fromHubId;
    private final UUID toHubId;
    private final int duration;
    private final BigDecimal distance;

    public static HubRouteDto from(HubRoute hubRoute) {
        return HubRouteDto.builder()
                .routeId(hubRoute.getRouteId())
                .fromHubId(hubRoute.getFromHubId())
                .toHubId(hubRoute.getToHubId())
                .duration(hubRoute.getDuration())
                .distance(hubRoute.getDistance())
                .build();
    }
}
