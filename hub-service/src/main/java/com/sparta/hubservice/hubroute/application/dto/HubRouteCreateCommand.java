package com.sparta.hubservice.hubroute.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class HubRouteCreateCommand {
    private final UUID fromHubId;
    private final UUID toHubId;
    private final Integer duration;
    private final BigDecimal distance;
}
