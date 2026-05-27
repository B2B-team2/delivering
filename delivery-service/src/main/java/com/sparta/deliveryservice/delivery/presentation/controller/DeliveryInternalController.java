package com.sparta.deliveryservice.delivery.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.deliveryservice.delivery.application.service.DeliveryService;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryOrderCancelRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryOrderCancelResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/internal")
@RequiredArgsConstructor
public class DeliveryInternalController {

    private final DeliveryService deliveryService;

    @PostMapping("/deliveries")
    public ResponseEntity<ApiResponse<List<DeliveryCreateResponse>>> createInternalDelivery(
            @RequestBody List<DeliveryCreateClientRequest> requests) {
        List<DeliveryCreateResponse> response = deliveryService.createDelivery(requests, "TEST_USER");
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/deliveries/cancel")
    public DeliveryOrderCancelResponse cancelDeliveriesByOrderId(@RequestBody List<DeliveryOrderCancelRequest> request) {
        DeliveryOrderCancelResponse response = deliveryService.cancelDeliveriesByOrderId(request);
        return response;
    }
}