package com.sparta.hubservice.hub.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LocationDto {

    @NotNull(message = "위도는 필수 입력 항목입니다.")
    private Double latitude;

    @NotNull(message = "경도는 필수 입력 항목입니다.")
    private Double longitude;
}
