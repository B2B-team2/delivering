package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.orderservice.order.infrastructure.client.dto.DefaultDeliveryAddressResponse;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "company-service")
public interface CompanyClient {

    // 업체 ID 배열 → 허브 ID 일괄 매핑 조회
    @PostMapping("/api/v1/internal/companies/hub-mapping")
    HubMappingResponse getHubMapping(@RequestBody HubMappingRequest request);

    // 수령업체의 기본 배송지(is_default=true) 조회
    @GetMapping("/api/v1/internal/companies/{companyId}/default-address")
    DefaultDeliveryAddressResponse getDefaultDeliveryAddress(@PathVariable UUID companyId);
}
