package com.sparta.hubservice.hubroute.presentation.dto;

import com.sparta.hubservice.hubroute.application.dto.HubRouteCreateCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class HubRouteCreateRequest {

    @NotNull
    private UUID fromHubId;

    @NotNull
    private UUID toHubId;

    @NotNull
    private Integer duration;

    @NotNull
    private BigDecimal distance;

    public HubRouteCreateCommand toCommand() {
        return HubRouteCreateCommand.builder()
                .fromHubId(fromHubId)
                .toHubId(toHubId)
                .duration(duration)
                .distance(distance)
                .build();
    }
}
