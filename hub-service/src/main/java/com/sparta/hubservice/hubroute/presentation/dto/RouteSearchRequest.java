package com.sparta.hubservice.hubroute.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RouteSearchRequest {

    @NotNull
    private UUID fromHubId;

    @NotNull
    private UUID toHubId;
}
