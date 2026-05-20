package com.sparta.hubservice.hub.presentation.dto;

import com.sparta.hubservice.hub.domain.core.Hub;
import com.sparta.hubservice.hub.domain.core.HubStatus;
import com.sparta.hubservice.hub.domain.core.HubType;

import java.io.Serializable;
import java.util.UUID;

public record HubResponse(
        UUID hubId,
        String name,
        HubType hubType,
        String address,
        Double latitude,
        Double longitude,
        String contactPhone,
        HubStatus status
) implements Serializable {

    public static HubResponse from(Hub hub) {
        return new HubResponse(
                hub.getHubId(),
                hub.getName(),
                hub.getHubType(),
                hub.getAddress(),
                hub.getLatitude(),
                hub.getLongitude(),
                hub.getContactPhone(),
                hub.getStatus()
        );
    }
}
