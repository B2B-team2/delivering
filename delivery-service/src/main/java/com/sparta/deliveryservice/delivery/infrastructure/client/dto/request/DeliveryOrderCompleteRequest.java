package com.sparta.deliveryservice.delivery.infrastructure.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOrderCompleteRequest {
    private String deliveredStatus;
    private String orderStatus;
}
