package com.sparta.hubservice.hubroute.presentation.dto;

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
}
