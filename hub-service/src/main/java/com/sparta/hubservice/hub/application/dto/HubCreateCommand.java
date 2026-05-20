package com.sparta.hubservice.hub.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HubCreateCommand {
    private final String name;
    private final String hubType;       // Service에서 HubType enum으로 변환
    private final String address;
    private final Double latitude;
    private final Double longitude;
    private final String contactPhone;
}
