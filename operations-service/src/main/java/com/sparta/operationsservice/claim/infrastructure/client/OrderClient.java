package com.sparta.operationsservice.claim.infrastructure.client;

import com.sparta.operationsservice.claim.infrastructure.client.dto.CompanyOrderDetailsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "order-service")
public interface OrderClient {

    @GetMapping("/api/v1/internal/orders/company/{companyOrderId}/details")
    CompanyOrderDetailsResponse getCompanyOrderDetails(@PathVariable UUID companyOrderId);

    @PatchMapping("/api/v1/internal/orders/company/{companyOrderId}/claim-cancel")
    void cancelCompanyOrderByClaim(
            @PathVariable UUID companyOrderId,
            @RequestHeader("X-User-Id") UUID requesterId
    );
}
