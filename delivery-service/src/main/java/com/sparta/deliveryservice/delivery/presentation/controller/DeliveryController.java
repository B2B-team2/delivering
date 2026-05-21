package com.sparta.deliveryservice.delivery.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.deliveryservice.delivery.application.service.DeliveryService;
import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryCreateRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCreateResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequiredArgsConstructor
@RestController("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;

//    @PostMapping("/deliveries")
//    public DeliveryCreateResponse createDelivery(@RequestBody DeliveryCreateRequest deliveryCreateRequest) {
//
//        return ApiResponse<createDelivery()>;
//    }
}
