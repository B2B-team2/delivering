package com.sparta.hubservice.hubroute.presentation.dto;

import com.sparta.hubservice.hubroute.application.dto.HubRouteUpdateCommand;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class HubRouteUpdateRequest {

    private Integer duration;
    private BigDecimal distance;

    public HubRouteUpdateCommand toCommand() {
        return HubRouteUpdateCommand.builder()
                .duration(duration)
                .distance(distance)
                .build();
    }
}
