package com.sparta.hubservice.hub.presentation.dto;

import com.sparta.hubservice.hub.application.dto.HubUpdateCommand;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HubUpdateRequest {

    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private String status;              // "ACTIVE" / "INACTIVE" / "MAINTENANCE"

    public HubUpdateCommand toCommand() {
        return HubUpdateCommand.builder()
                .name(name)
                .address(address)
                .latitude(latitude)
                .longitude(longitude)
                .contactPhone(contactPhone)
                .status(status)
                .build();
    }
}
