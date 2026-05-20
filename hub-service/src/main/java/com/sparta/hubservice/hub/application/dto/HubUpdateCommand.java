package com.sparta.hubservice.hub.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HubUpdateCommand {
    private final String name;
    private final String address;
    private final Double latitude;
    private final Double longitude;
    private final String contactPhone;
    private final String status;        // Service에서 HubStatus enum으로 변환
}
