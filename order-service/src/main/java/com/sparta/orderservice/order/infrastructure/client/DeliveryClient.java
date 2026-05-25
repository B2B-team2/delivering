package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.ApiResponse;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {

    // 배송 일괄 생성: CompanyOrder 목록을 한 번에 전송
    @PostMapping("/internal/deliveries")
    ApiResponse<List<DeliveryCreateResponse>> createDeliveries(@RequestBody List<DeliveryCreateRequest> requests);
}
