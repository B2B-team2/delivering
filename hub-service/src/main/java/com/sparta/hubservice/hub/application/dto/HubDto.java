package com.sparta.hubservice.hub.application.dto;

import com.sparta.hubservice.hub.domain.core.Hub;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class HubDto {
    private final UUID hubId;
    private final String name;
    private final String hubType;
    private final String address;
    private final Double latitude;
    private final Double longitude;
    private final String contactPhone;
    private final String status;

    public static HubDto from(Hub hub) {
        return HubDto.builder()
                .hubId(hub.getHubId())
                .name(hub.getName())
                .hubType(hub.getHubType().name())
                .address(hub.getAddress())
                .latitude(hub.getLatitude())
                .longitude(hub.getLongitude())
                .contactPhone(hub.getContactPhone())
                .status(hub.getStatus().name())
                .build();
    }
}
