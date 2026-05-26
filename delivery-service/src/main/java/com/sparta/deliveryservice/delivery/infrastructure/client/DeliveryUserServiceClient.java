package com.sparta.deliveryservice.delivery.infrastructure.client;

import com.sparta.common.dto.ApiResponse; // 공통 ApiResponse 패키지 경로에 맞게 지정
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryManagerResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

@FeignClient(name = "user-service")
public interface DeliveryUserServiceClient {

    @GetMapping("/api/v1/internal/users/{delivery_id}/manager-info")
    DeliveryManagerResponse getManagerInfo(@PathVariable("delivery_id") UUID deliveryId);
}