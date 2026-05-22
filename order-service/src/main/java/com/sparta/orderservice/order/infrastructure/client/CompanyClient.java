package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.ApiResponse;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.HubMappingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "company-service")
public interface CompanyClient {

    // 업체 ID 배열 → 허브 ID 일괄 매핑 조회
    @PostMapping("/api/v1/internal/companies/hub-mapping")
    ApiResponse<HubMappingResponse> getHubMapping(@RequestBody HubMappingRequest request);
}
