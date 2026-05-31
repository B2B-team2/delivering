package com.sparta.deliveryservice.delivery.presentation.dto.request;

import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryCreateRequest {

    @NotNull(message = "주문 ID는 필수 입력값입니다.")
    private UUID companyOrderId;

    @NotNull(message = "출발 허브 ID는 필수 입력값입니다.")
    private UUID departureHubId;

    @NotNull(message = "도착 허브 ID는 필수 입력값입니다.")
    private UUID destinationHubId;

    @NotBlank(message = "배송 주소는 필수 입력값입니다.")
    @Size(max = 255, message = "배송 주소는 255자 이하로 입력해주세요.")
    private DeliveryAddress deliveryAddress;

    @NotBlank(message = "수령인 이름은 필수 입력값입니다.")
    @Size(max = 100, message = "수령인 이름은 100자 이하로 입력해주세요.")
    private String recipientName;

    @Size(max = 100, message = "수령인 Slack ID는 100자 이하로 입력해주세요.")
    private String recipientSlackId;

    private String memo;

    @NotEmpty(message = "배송 경로는 최소 1개 이상 입력해야 합니다.")
    @Valid
    private List<DeliveryRouteCreateDto> routes;

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class DeliveryRouteCreateDto {
        @NotNull(message = "경로 순번은 필수 입력값입니다.")
        private Integer sequence;

        @NotNull(message = "출발 허브 ID는 필수 입력값입니다.")
        private UUID fromHubId;

        @NotNull(message = "도착 허브 ID는 필수 입력값입니다.")
        private UUID toHubId;

        private BigDecimal estimatedDistance;
        private String estimatedDuration;
    }
}