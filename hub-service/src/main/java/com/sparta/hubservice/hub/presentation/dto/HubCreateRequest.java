package com.sparta.hubservice.hub.presentation.dto;

import com.sparta.hubservice.hub.domain.core.HubType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HubCreateRequest(
        @NotBlank String name,
        @NotNull HubType hubType,
        @NotBlank String address,
        @NotNull Double latitude,
        @NotNull Double longitude,
        String contactPhone
) {}
