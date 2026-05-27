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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryRouteController {

    private final DeliveryRouteService deliveryRouteService;

    @GetMapping("/deliveries/{delivery_id}/routes")
    public ResponseEntity<ApiResponse<DeliveryRouteDetailResponse>> getDeliveryDetailRoutes(@PathVariable("delivery_id") UUID deliveryId, @RequestHeader("X-User-Id")  UUID userId) {
        DeliveryRouteDetailResponse response = deliveryRouteService.getDeliveryDetailRoutes(deliveryId, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/deliveries/{delivery_id}/routes/{route_id}")
    public ResponseEntity<ApiResponse<DeliveryRouteStatusUpdateResponse>> getDeliveryRouteStatusUpdate(@PathVariable("delivery_id") UUID deliveryId, @PathVariable("route_id") UUID routeId, @RequestHeader("X-User-Id")  UUID userId, @RequestBody DeliveryRouteStatusUpdateRequest request) throws JsonProcessingException {
        DeliveryRouteStatusUpdateResponse response = deliveryRouteService.updateRouteStatus(deliveryId, routeId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/deliveries/{delivery_id}/routes/{route_id}")
    public ResponseEntity<ApiResponse<DeliveryRouteDeleteResponse>> getDeliveryRouteStatusDelete(@PathVariable("delivery_id") UUID deliveryId, @PathVariable("route_id") UUID routeId, @RequestHeader("X-User-Id")  UUID userId, @RequestBody DeliveryRouteDeleteRequest request) throws JsonProcessingException {
        DeliveryRouteDeleteResponse response = deliveryRouteService.deleteRoute(deliveryId, routeId, userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}