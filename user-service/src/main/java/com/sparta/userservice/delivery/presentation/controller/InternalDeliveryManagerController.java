package com.sparta.userservice.delivery.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.userservice.delivery.application.service.DeliveryManagerService;
import com.sparta.userservice.delivery.presentation.dto.response.InternalDeliveryManagerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
@Tag(name = "Internal", description = "내부 서비스 통신 API")
public class InternalDeliveryManagerController {

    private final DeliveryManagerService deliveryManagerService;

    @Operation(summary = "배송담당자 배정 (내부용)")
    @PostMapping("/manager-info")
    public ResponseEntity<ApiResponse<InternalDeliveryManagerResponse>> getManagerInfo(
            @RequestBody UUID fromHubId) {
        return ResponseEntity.ok(ApiResponse.success(
                deliveryManagerService.assignByHub(fromHubId)));
    }
}