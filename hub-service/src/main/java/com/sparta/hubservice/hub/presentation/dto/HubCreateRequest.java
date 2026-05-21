package com.sparta.hubservice.hub.presentation.dto;

import com.sparta.hubservice.hub.application.dto.HubCreateCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HubCreateRequest {

    @NotBlank(message = "허브 이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "허브 타입은 필수 입력 항목입니다.")
    private String hubType;   // "REGIONAL" / "CENTRAL"

    @NotBlank(message = "주소는 필수 입력 항목입니다.")
    private String address;

    @Valid
    @NotNull(message = "위치 정보는 필수 입력 항목입니다.")
    private LocationDto location;

    private String contactPhone;

    public HubCreateCommand toCommand() {
        return HubCreateCommand.builder()
                .name(name)
                .hubType(hubType)
                .address(address)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .contactPhone(contactPhone)
                .build();
    }
}
