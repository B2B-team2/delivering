package com.sparta.orderservice.order.infrastructure.feign;

import com.sparta.common.dto.ApiResponse;
import com.sparta.orderservice.order.infrastructure.feign.dto.CompanyHubMappingRequest;
import com.sparta.orderservice.order.infrastructure.feign.dto.CompanyHubMappingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @PostMapping("/api/v1/internal/companies/hub-mapping")
    ApiResponse<CompanyHubMappingResponse> getHubMapping(@RequestBody CompanyHubMappingRequest request);
}
