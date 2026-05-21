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
    private LocationDto location;
    private String contactPhone;
    private String status;

    public static HubResponse from(HubDto dto) {
        return HubResponse.builder()
                .hubId(dto.getHubId())
                .name(dto.getName())
                .hubType(dto.getHubType())
                .address(dto.getAddress())
                .location(new LocationDto(dto.getLatitude(), dto.getLongitude()))
                .contactPhone(dto.getContactPhone())
                .status(dto.getStatus())
                .build();
    }
}
