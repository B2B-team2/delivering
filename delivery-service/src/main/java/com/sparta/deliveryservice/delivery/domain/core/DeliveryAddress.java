package com.sparta.deliveryservice.delivery.domain.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DeliveryAddress {
    private String address;
    private String addressDetail;
}