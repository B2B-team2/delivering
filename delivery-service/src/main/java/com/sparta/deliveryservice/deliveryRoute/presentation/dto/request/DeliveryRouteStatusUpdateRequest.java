package com.sparta.deliveryservice.deliveryRoute.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryRouteStatusUpdateRequest {

    @NotNull(message = "변경할 경로 상태값은 필수입니다.")
    private String status;

    @NotBlank(message = "상태 변경 사유는 필수 입력값입니다.")
    private String reason;

    private BigDecimal actualDistance;
    private String actualDuration;
}