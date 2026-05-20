package com.sparta.hubservice.hub.presentation.dto;

import com.sparta.hubservice.hub.domain.core.HubStatus;

public record HubUpdateRequest(
        String name,
        String address,
        Double latitude,
        Double longitude,
        String contactPhone,
        HubStatus status
) {}
