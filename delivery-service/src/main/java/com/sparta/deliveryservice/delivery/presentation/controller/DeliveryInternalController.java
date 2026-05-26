package com.sparta.deliveryservice.delivery.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.deliveryservice.delivery.application.service.DeliveryService;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryOrderCancelRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryOrderCancelResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCreateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<List<DeliveryCreateResponse>> createInternalDeliveries(@Valid @RequestBody DeliveryCreateClientRequest request, @RequestHeader(value = "X-User-Id", required = false, defaultValue = "c7e2b1a0-5678-4def-9012-3456789abcde") String userId) {
        List<DeliveryCreateResponse> responses = deliveryService.createSingleDeliveryTransaction(request, userId);
        return ApiResponse.created(responses);
    }

    @PostMapping("/deliveries/cancel")
    public DeliveryOrderCancelResponse cancelDeliveriesByOrderId(@RequestBody DeliveryOrderCancelRequest request) {
        DeliveryOrderCancelResponse response = deliveryService.cancelDeliveriesByOrderId(request.getOrderId());
        return response;
    }
}