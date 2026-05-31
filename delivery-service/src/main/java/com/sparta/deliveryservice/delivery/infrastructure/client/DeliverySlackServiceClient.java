package com.sparta.deliveryservice.delivery.infrastructure.client;

import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.SlackMessageSendRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "operations-service")
public interface DeliverySlackServiceClient {

    @PostMapping("/api/v1/slack/send")
    void sendSlackMessage(@RequestBody SlackMessageSendRequest request);
}