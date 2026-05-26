package com.sparta.deliveryservice.delivery.infrastructure.client;

import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryHubRouteSearchRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryHubRouteSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CachedHubServiceClient {

    private final DeliveryHubServiceClient deliveryHubServiceClient;

    @Cacheable(
            value = "hubRoute",
            key = "#request.fromHubId.toString() + ':' + #request.toHubId.toString()",
            unless = "#result == null"
    )
    public DeliveryHubRouteSearchResponse getHubRouteWithCache(DeliveryHubRouteSearchRequest request) {
        return deliveryHubServiceClient.searchHubRoutes(request);
    }
}