package com.sparta.deliveryservice.deliveryRoute.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.deliveryservice.deliveryRoute.application.service.DeliveryRouteService;
import com.sparta.deliveryservice.deliveryRoute.domin.core.DeliveryRoute;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteCreateRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteCreateResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDetailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    @PostMapping("/delivery-routes")
    public ApiResponse<DeliveryRouteCreateResponse> create(@Valid @RequestBody DeliveryRouteCreateRequest deliveryRouteCreateRequest) {
        DeliveryRouteCreateResponse response = deliveryRouteService.createDeliveryRoute(deliveryRouteCreateRequest);
        return ApiResponse.created(response);
    }

    @GetMapping("/deliveries/{deliveryId}/routes")
    public ApiResponse<DeliveryRouteDetailResponse> getDeliveryDetailRoutes(@PathVariable("deliveryId") UUID deliveryId) {
        DeliveryRouteDetailResponse response = deliveryRouteService.getDeliveryDetailRoutes(deliveryId);
        return ApiResponse.success(response);
    }


}