package com.sparta.deliveryservice.delivery.infrastructure.client;

import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryManagerRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryManagerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface DeliveryUserServiceClient {

    @PostMapping("/api/v1/internal/users/manager-info")
    DeliveryManagerResponse getManagerInfo(@RequestBody UUID fromHubId);
}