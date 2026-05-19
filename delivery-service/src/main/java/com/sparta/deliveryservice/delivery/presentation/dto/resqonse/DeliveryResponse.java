package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;
import com.sparta.common.dto.PageResponse;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class DeliveryResponse {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeliveryResponseDto {
        private UUID deliveryId;
        private String trackingNumber;
        private String status;
        private String departureHubName;
        private String destinationHubName;
        private String deliveryAddress;
        private String recipientName;
        private UUID deliveryManagerId;
        private String deliveryManagerName;
        private LocalDateTime finalDispatchDeadlineAt;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private LocalDateTime createdAt;
    }

    public static PageResponse<DeliveryResponseDto> of(Page<DeliveryResponseDto> pageInfo) {
        return new PageResponse<>(pageInfo);
    }
}