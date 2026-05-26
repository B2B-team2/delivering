package com.sparta.deliveryservice.delivery.infrastructure.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryManagerResponse {
    private UUID deliveryManagerId;
    private String deliverySlackId;
    private String managerName;
    private String managerPhone;
}