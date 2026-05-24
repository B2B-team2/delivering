package com.sparta.deliveryservice.deliveryRoute.presentation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.common.dto.ApiResponse;
import com.sparta.deliveryservice.deliveryRoute.application.service.DeliveryRouteService;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteDeleteRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.request.DeliveryRouteStatusUpdateRequest;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDeleteResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteDetailResponse;
import com.sparta.deliveryservice.deliveryRoute.presentation.dto.resqonse.DeliveryRouteStatusUpdateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    @GetMapping("/deliveries/{delivery_id}/routes")
    public ApiResponse<DeliveryRouteDetailResponse> getDeliveryDetailRoutes(@PathVariable("delivery_id") UUID deliveryId) {
        DeliveryRouteDetailResponse response = deliveryRouteService.getDeliveryDetailRoutes(deliveryId);
        return ApiResponse.success(response);
    }

    @PatchMapping("/deliveries/{delivery_id}/routes/{route_id}")
    public ApiResponse<DeliveryRouteStatusUpdateResponse> getDeliveryRouteStatusUpdate(@PathVariable("delivery_id") UUID deliveryId, @PathVariable("route_id") UUID routeId, @RequestBody DeliveryRouteStatusUpdateRequest request) throws JsonProcessingException {
        DeliveryRouteStatusUpdateResponse response = deliveryRouteService.updateRouteStatus(deliveryId, routeId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/deliveries/{delivery_id}/routes/{route_id}")
    public ApiResponse<DeliveryRouteDeleteResponse> getDeliveryRouteStatusDelete(@PathVariable("delivery_id") UUID deliveryId, @PathVariable("route_id") UUID routeId, @RequestBody DeliveryRouteDeleteRequest request) throws JsonProcessingException {
        DeliveryRouteDeleteResponse response = deliveryRouteService.deleteRoute(deliveryId, routeId, request);
        return ApiResponse.success(response);
    }

}