package com.sparta.deliveryservice.delivery.infrastructure.client;

import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.HubRouteSearchRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.HubRouteSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service")
public interface HubServiceClient {

    @PostMapping("/api/v1/internal/hub-routes/search")
    HubRouteSearchResponse searchHubRoutes(@RequestBody HubRouteSearchRequest request);
}