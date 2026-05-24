package com.sparta.hubservice.hubroute.presentation.dto;

import com.sparta.hubservice.hubroute.application.dto.HubRouteDto;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class HubRouteResponse {

    private final UUID routeId;
    private final HubInfo fromHub;
    private final HubInfo toHub;
    private final String duration;
    private final BigDecimal distance;
    private final LocalDateTime createdAt;
    private final String createdBy;
    private final LocalDateTime updatedAt;
    private final String updatedBy;

    public static HubRouteResponse from(HubRouteDto dto) {
        return HubRouteResponse.builder()
                .routeId(dto.getRouteId())
                .fromHub(HubInfo.from(dto.getFromHub()))
                .toHub(HubInfo.from(dto.getToHub()))
                .duration(dto.getDuration())
                .distance(dto.getDistance())
                .createdAt(dto.getCreatedAt())
                .createdBy(dto.getCreatedBy())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }

    @Getter
    @Builder
    public static class HubInfo {
        private final UUID hubId;
        private final String name;
        private final String address;

        public static HubInfo from(HubRouteDto.HubInfoDto dto) {
            if (dto == null) return null;
            return HubInfo.builder()
                    .hubId(dto.getHubId())
                    .name(dto.getName())
                    .address(dto.getAddress())
                    .build();
        }
    }
}
