package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCancelRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "delivery-service")
public interface DeliveryClient {

    // 배송 일괄 생성: CompanyOrder 목록을 한 번에 전송
    @PostMapping("/api/v1/internal/deliveries")
    List<DeliveryCreateResponse> createDeliveries(@RequestBody List<DeliveryCreateRequest> requests);

    // 배송 일괄 취소 (Saga 보상 전용 내부 API)
    // TODO: 배송팀 구현 완료 후 연동 - POST /api/v1/internal/deliveries/cancel
    @PostMapping("/api/v1/internal/deliveries/cancel")
    void cancelDeliveries(@RequestBody DeliveryCancelRequest request);
}
