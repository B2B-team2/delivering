package com.sparta.orderservice.order.infrastructure.feign;

import com.sparta.common.dto.ApiResponse;
import com.sparta.orderservice.order.infrastructure.feign.dto.CompanyDefaultAddressResponse;
import com.sparta.orderservice.order.infrastructure.feign.dto.CompanyHubMappingRequest;
import com.sparta.orderservice.order.infrastructure.feign.dto.CompanyHubMappingResponse;
import com.sparta.orderservice.order.infrastructure.feign.dto.ProductOptionDetailsRequest;
import com.sparta.orderservice.order.infrastructure.feign.dto.ProductOptionDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @PostMapping("/api/v1/internal/companies/hub-mapping")
    ResponseEntity<ApiResponse<CompanyHubMappingResponse>> getHubMapping(@RequestBody CompanyHubMappingRequest request);

    @GetMapping("/api/v1/internal/companies/{companyId}/default-address")
    ResponseEntity<ApiResponse<CompanyDefaultAddressResponse>> getDefaultAddress(@PathVariable("companyId") UUID companyId);

    @PostMapping("/api/v1/internal/product-options/details")
    ResponseEntity<ApiResponse<ProductOptionDetailsResponse>> getProductOptionDetails(@RequestBody ProductOptionDetailsRequest request);
}
