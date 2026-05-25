package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import com.sparta.common.dto.PageResponse;
import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeliverySearchResponse {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeliveryResponseDto {
        private UUID deliveryId;
        private String trackingNumber;
        private String status;
        private UUID departureHubId;
        private UUID destinationHubId;
        private DeliveryAddress deliveryAddress;
        private String recipientName;
        private UUID deliveryManagerId;
        private LocalDateTime finalDispatchDeadlineAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
    }

    public static PageResponse<DeliveryResponseDto> of(Page<DeliveryResponseDto> pageInfo) {
        return new PageResponse<>(pageInfo);
    }
}