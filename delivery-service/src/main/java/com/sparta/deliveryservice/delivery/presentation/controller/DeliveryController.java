package com.sparta.deliveryservice.delivery.presentation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.deliveryservice.delivery.application.service.DeliveryService;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryCancelRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryStatusUpdateRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryAddressResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCancelResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryDetailResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliverySearchResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryStatusResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryStatusUpdateResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryTrackingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/deliveries")
    public ApiResponse<PageResponse<DeliverySearchResponse.DeliveryResponseDto>> searchDeliveries(@PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<DeliverySearchResponse.DeliveryResponseDto> deliveryPage = deliveryService.searchDeliveries(pageable);
        PageResponse<DeliverySearchResponse.DeliveryResponseDto> response = DeliverySearchResponse.of(deliveryPage);
        return ApiResponse.success(response);
    }

    @GetMapping("/deliveries/{delivery_id}")
    public ApiResponse<DeliveryDetailResponse> getDeliveryDetail(@PathVariable("delivery_id") UUID deliveryId) {
        DeliveryDetailResponse response = deliveryService.getDeliveryDetail(deliveryId);
        return ApiResponse.success(response);
    }

    @GetMapping("/delivery-addresses/{delivery_id}/{address_id}")
    public ApiResponse<DeliveryAddressResponse> getDeliveryAddress(@PathVariable("delivery_id") UUID deliveryId, @PathVariable("address_id") UUID addressId) {
        DeliveryAddressResponse response = deliveryService.getDeliveryAddress(deliveryId, addressId);
        return ApiResponse.success(response);
    }

    @GetMapping("/deliveries/tracking/{tracking_number}")
    public ApiResponse<DeliveryTrackingResponse> getDeliveryTracking(@PathVariable("tracking_number") String trackingNumber) {
        DeliveryTrackingResponse response = deliveryService.trackDelivery(trackingNumber);
        return ApiResponse.success(response);
    }

    @PatchMapping("/deliveries/{delivery_id}/status")
    public ApiResponse<DeliveryStatusUpdateResponse> getDeliveryStatus(@PathVariable("delivery_id") UUID deliveryId, @RequestBody DeliveryStatusUpdateRequest request) throws JsonProcessingException {
        DeliveryStatusUpdateResponse response = deliveryService.updateDeliveryStatus(deliveryId, request);
        return ApiResponse.success(response);
    }
    @PutMapping("/deliveries/{deliveryId}/cancel")
    public ApiResponse<DeliveryCancelResponse> cancelDelivery(
            @PathVariable("deliveryId") UUID deliveryId,
            @Valid @RequestBody DeliveryCancelRequest request) {
        DeliveryCancelResponse response = deliveryService.cancelDelivery(deliveryId, request);
        return ApiResponse.success(response);
    }

    @PatchMapping("/start/{trackingNumber}")
    public ApiResponse<DeliveryStatusResponse> startDelivery(@PathVariable("trackingNumber") String trackingNumber) {
        DeliveryStatusResponse response = deliveryService.startDelivery(trackingNumber);
        return ApiResponse.success(response);
    }

    @PatchMapping("/complete/{trackingNumber}")
    public ApiResponse<DeliveryStatusResponse> completeDelivery(@PathVariable("trackingNumber") String trackingNumber) {
        DeliveryStatusResponse response = deliveryService.completeDelivery(trackingNumber);
        return ApiResponse.success(response);
    }

}
