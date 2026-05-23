package com.sparta.deliveryservice.delivery.infrastructure.client.dto.request;

import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
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
public class DeliveryCreateClientRequest {
    private UUID companyOrderId;
    private UUID companyReceiveId;
    private UUID departureHubId;
    private UUID destinationHubId;
    private DeliveryAddress deliveryAddress;
    private String phone;
    private String postalCode;
    private String recipientName;
    private String recipientSlackId;
    private String memo;
}