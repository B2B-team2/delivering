package com.sparta.userservice.delivery.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.userservice.delivery.application.service.DeliveryManagerService;
import com.sparta.userservice.delivery.presentation.dto.request.DeliveryManagerAssignRequest;
import com.sparta.userservice.delivery.presentation.dto.request.DeliveryManagerStatusRequest;
import com.sparta.userservice.delivery.presentation.dto.response.DeliveryManagerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.sparta.userservice.user.domain.enums.ManagerType;
import com.sparta.userservice.user.domain.enums.DeliveryManagerStatus;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/delivery-managers")
@RequiredArgsConstructor
@Tag(name = "DeliveryManager", description = "배송담당자 API")
public class DeliveryManagerController {

    private final DeliveryManagerService deliveryManagerService;

    @Operation(summary = "배송담당자 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DeliveryManagerResponse>>> getAllDeliveryManagers(
            @RequestParam(required = false) ManagerType managerType,
            @RequestParam(required = false) DeliveryManagerStatus status,
            @RequestParam(required = false) UUID hubId,
            Pageable pageable) {
        pageable = PageableUtil.validatePageSize(pageable);
        return ResponseEntity.ok(ApiResponse.success(
                deliveryManagerService.getAllDeliveryManagers(managerType, status, hubId, pageable)));
    }

    @Operation(summary = "배송담당자 단건 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<DeliveryManagerResponse>> getDeliveryManager(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryManagerService.getDeliveryManager(userId)));
    }

    @Operation(summary = "배송담당자 배정")
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<DeliveryManagerResponse>> assign(
            @RequestBody DeliveryManagerAssignRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryManagerService.assign(request)));
    }

    @Operation(summary = "배송담당자 상태 변경")
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<DeliveryManagerResponse>> updateStatus(
            @PathVariable UUID userId,
            @RequestBody DeliveryManagerStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryManagerService.updateStatus(userId, request)));
    }
}