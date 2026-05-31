package com.sparta.operationsservice.claim.infrastructure.client;

import com.sparta.operationsservice.claim.infrastructure.client.dto.InventoryBulkRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service")
public interface HubClient {

    @PostMapping("/api/v1/internal/inventory/return")
    void returnStock(@RequestBody InventoryBulkRequest request);

    @PostMapping("/api/v1/internal/inventory/deduct")
    void deductStock(@RequestBody InventoryBulkRequest request);
}
