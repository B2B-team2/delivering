package com.sparta.deliveryservice.delivery.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAddress {
    private String address;
    private String addressDetail;
}