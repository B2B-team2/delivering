package com.sparta.deliveryservice.delivery.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryStatusUpdateRequest {

    @NotNull(message = "변경할 배송 상태값은 필수입니다.")
    private String status;

    @NotBlank(message = "상태 변경 사유는 필수 입력값입니다.")
    private String reason;
}