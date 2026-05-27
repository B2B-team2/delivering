package com.sparta.hubservice.global.client;

import com.sparta.hubservice.global.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "order-service", configuration = FeignConfig.class)
public interface OrderClient {

    @PatchMapping("/api/v1/internal/orders/company/{companyOrderId}/preparing")
    void prepareCompanyOrder(@PathVariable("companyOrderId") UUID companyOrderId);

    @PatchMapping("/api/v1/internal/orders/company/{companyOrderId}/shipped")
    void shipCompanyOrder(@PathVariable("companyOrderId") UUID companyOrderId);
}
