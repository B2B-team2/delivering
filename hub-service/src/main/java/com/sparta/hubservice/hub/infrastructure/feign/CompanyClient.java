package com.sparta.hubservice.hub.infrastructure.feign;

import com.sparta.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "company-service")
public interface CompanyClient {

    @GetMapping("/api/v1/internal/companies/exists")
    ApiResponse<Boolean> existsCompanyInHub(@RequestParam(name = "hubId") UUID hubId);
}
