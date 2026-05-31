package com.sparta.deliveryservice.delivery.infrastructure.client;


import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryAiCreateRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryAiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "operations-service")
public interface DeliveryAiServiceClient {

    @PostMapping("/api/v1/ai/generate")
    DeliveryAiResponse generateAiDescription(@RequestBody DeliveryAiCreateRequest request);
}