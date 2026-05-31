package com.sparta.deliveryservice.delivery.presentation.dto.resqonse;

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
public class DeliveryAddressResponse {
    private UUID addressId;
    private UUID companyId;
    private String recipientName;
    private String phone;
    private String address;
    private String addressDetail;
    private String postalCode;
}