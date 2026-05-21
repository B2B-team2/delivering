package com.sparta.deliveryservice.deliveryRoute.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteCreateRequest {

    @NotNull(message = "배송 ID는 필수 입력값입니다.")
    private UUID deliveryId;

    @NotNull(message = "경로 순번은 필수 입력값입니다.")
    private Integer sequence;

    @NotNull(message = "출발 허브 ID는 필수 입력값입니다.")
    private UUID fromHubId;

    @NotNull(message = "도착 허브 ID는 필수 입력값입니다.")
    private UUID toHubId;

    private BigDecimal estimatedDistance;
    private LocalDateTime estimatedDuration;
}