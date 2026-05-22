package com.sparta.hubservice.hub.application.dto;

import com.sparta.hubservice.hub.domain.core.Hub;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
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
    private final LocalDateTime createdAt;
    private final String createdBy;
    private final LocalDateTime updatedAt;
    private final String updatedBy;

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
                .createdAt(hub.getCreatedAt())
                .createdBy(hub.getCreatedBy())
                .updatedAt(hub.getUpdatedAt())
                .updatedBy(hub.getUpdatedBy())
                .build();
    }
}
