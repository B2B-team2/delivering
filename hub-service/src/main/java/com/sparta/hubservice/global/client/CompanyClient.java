package com.sparta.hubservice.global.client;

import com.sparta.hubservice.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "company-service", configuration = FeignConfig.class)
public interface CompanyClient {

    @GetMapping("/api/v1/internal/companies/exists")
    Boolean existsCompaniesByHubId(@RequestParam("hubId") UUID hubId);
}
