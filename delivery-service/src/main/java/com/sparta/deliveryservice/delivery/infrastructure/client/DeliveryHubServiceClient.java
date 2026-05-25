package com.sparta.deliveryservice.delivery.infrastructure.client;

import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryHubRouteSearchRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryHubRouteSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service")
public interface DeliveryHubServiceClient {

    @PostMapping("/api/v1/internal/hub-routes/search")
    DeliveryHubRouteSearchResponse searchHubRoutes(@RequestBody DeliveryHubRouteSearchRequest request);
}