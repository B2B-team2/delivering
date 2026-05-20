package com.sparta.hubservice.hub.presentation.dto;

import com.sparta.hubservice.hub.application.dto.HubDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HubResponse {

    private UUID hubId;
    private String name;
    private String hubType;
    private String address;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private String status;

    public static HubResponse from(HubDto dto) {
        return HubResponse.builder()
                .hubId(dto.getHubId())
                .name(dto.getName())
                .hubType(dto.getHubType())
                .address(dto.getAddress())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .contactPhone(dto.getContactPhone())
                .status(dto.getStatus())
                .build();
    }
}
