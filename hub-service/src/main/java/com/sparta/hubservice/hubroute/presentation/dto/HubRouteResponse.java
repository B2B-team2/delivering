package com.sparta.hubservice.hubroute.presentation.dto;

import com.sparta.hubservice.hubroute.application.dto.HubRouteDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class HubRouteResponse {

    private final UUID routeId;
    private final UUID fromHubId;
    private final UUID toHubId;
    private final int duration;
    private final BigDecimal distance;

    public static HubRouteResponse from(HubRouteDto dto) {
        return HubRouteResponse.builder()
                .routeId(dto.getRouteId())
                .fromHubId(dto.getFromHubId())
                .toHubId(dto.getToHubId())
                .duration(dto.getDuration())
                .distance(dto.getDistance())
                .build();
    }
}
