package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryStatusResponse {

    private UUID deliveryManagerId;
    private String deliveryManagerSlackId;
    private String deliveryManagerName;
    private String deliveryManagerPhone;
    private String trackingNumber;
    private String status;
    private LocalDateTime startedAt;
    private String address;
    private String addressDetail;
}