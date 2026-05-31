package com.sparta.deliveryservice.delivery.infrastructure.client;

import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryOrderCompleteRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(name = "order-service")
public interface DeliveryOrderServiceClient {

    @PatchMapping("/api/v1/internal/orders/company/{company_order_id}/delivered")
    void companyOrderDelivered(@PathVariable("company_order_id") UUID companyOrderId, @RequestHeader("X-User-Id") UUID requesterId, @RequestBody DeliveryOrderCompleteRequest request);
}
