package com.sparta.deliveryservice.delivery.presentation.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.deliveryservice.delivery.application.service.DeliveryService;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryCancelRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.request.DeliveryStatusUpdateRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryAddressResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCancelResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCreateResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryDetailResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliverySearchResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryStatusResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryStatusUpdateResponse;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryTrackingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @GetMapping("/deliveries")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<DeliverySearchResponse.DeliveryResponseDto>>> getDelivery(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<DeliverySearchResponse.DeliveryResponseDto> response = deliveryService.searchDeliveries(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/deliveries/{delivery_id}")
    public ResponseEntity<ApiResponse<DeliveryDetailResponse>> getDeliveryDetail(
            @PathVariable("delivery_id") UUID deliveryId) {
        DeliveryDetailResponse response = deliveryService.getDeliveryDetail(deliveryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/delivery-addresses/{delivery_id}/{address_id}")
    public ResponseEntity<ApiResponse<DeliveryAddressResponse>> getDeliveryAddress(
            @PathVariable("delivery_id") UUID deliveryId,
            @PathVariable("address_id") UUID addressId) {
        DeliveryAddressResponse response = deliveryService.getDeliveryAddress(deliveryId, addressId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/deliveries/tracking/{tracking_number}")
    public ResponseEntity<ApiResponse<DeliveryTrackingResponse>> getDeliveryTracking(
            @PathVariable("tracking_number") String trackingNumber) {
        DeliveryTrackingResponse response = deliveryService.trackDelivery(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/deliveries/{delivery_id}/status")
    public ResponseEntity<ApiResponse<DeliveryStatusUpdateResponse>> getDeliveryStatus(
            @PathVariable("delivery_id") UUID deliveryId,
            @RequestBody DeliveryStatusUpdateRequest request) throws JsonProcessingException {
        DeliveryStatusUpdateResponse response = deliveryService.updateDeliveryStatus(deliveryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/deliveries/{delivery_id}/cancel")
    public ResponseEntity<ApiResponse<DeliveryCancelResponse>> cancelDelivery(
            @PathVariable("delivery_id") UUID deliveryId,
            @Valid @RequestBody DeliveryCancelRequest request) {
        DeliveryCancelResponse response = deliveryService.cancelDelivery(deliveryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/deliveries/start/{trackingNumber}")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> startDelivery(
            @PathVariable("trackingNumber") String trackingNumber) {
        DeliveryStatusResponse response = deliveryService.startDelivery(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/deliveries/complete/{trackingNumber}")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> completeDelivery(
            @PathVariable("trackingNumber") String trackingNumber, @RequestHeader("X-User-Id") UUID userId) {
        DeliveryStatusResponse response = deliveryService.completeDelivery(trackingNumber, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/deliveries/{deliveryId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'COMPANY_DELIVERY_MANAGER')")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> cancelDelivery(
            @PathVariable("deliveryId") UUID deliveryId) {

        DeliveryStatusResponse response = deliveryService.deleteDelivery(deliveryId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
