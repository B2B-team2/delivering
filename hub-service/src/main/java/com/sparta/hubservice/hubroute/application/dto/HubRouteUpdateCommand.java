package com.sparta.hubservice.hubroute.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class HubRouteUpdateCommand {
    private final Integer duration;
    private final BigDecimal distance;
}
