package com.sparta.deliveryservice.delivery.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryCancelRequest {

    @NotBlank(message = "취소 사유는 필수 입력값입니다.")
    @Size(max = 255, message = "취소 사유는 255자 이하로 입력해주세요.")
    private String reason;
}