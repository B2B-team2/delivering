package com.sparta.orderservice.order.infrastructure.feign;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.internal.CompanyHubMappingRequest;
import com.sparta.common.dto.internal.CompanyHubMappingResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @PostMapping("/api/v1/internal/companies/hub-mapping")
    ResponseEntity<ApiResponse<CompanyHubMappingResponse>> getHubMapping(@RequestBody CompanyHubMappingRequest request);
}
