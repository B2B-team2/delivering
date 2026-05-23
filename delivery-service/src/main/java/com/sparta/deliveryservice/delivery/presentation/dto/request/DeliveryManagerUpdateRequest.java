package com.sparta.deliveryservice.delivery.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryManagerUpdateRequest {

    @NotNull(message = "변경할 배송 담당자 ID는 필수입니다.")
    private UUID deliveryManagerId;

    @NotBlank(message = "담당자 이름은 필수 입력값입니다.")
    private String name;

    @NotBlank(message = "담당자 전화번호는 필수 입력값입니다.")
    private String phone;

    @NotBlank(message = "담당자 변경 사유는 필수 입력값입니다.")
    private String reason;
}