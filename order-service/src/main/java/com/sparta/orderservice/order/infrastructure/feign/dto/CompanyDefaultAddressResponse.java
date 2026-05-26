package com.sparta.orderservice.order.infrastructure.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDefaultAddressResponse {
    private String address;
    private String addressDetail;
    private String recipientName;
    private String phone;
}
