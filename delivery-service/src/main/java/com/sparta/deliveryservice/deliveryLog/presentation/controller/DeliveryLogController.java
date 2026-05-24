package com.sparta.deliveryservice.deliveryLog.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.deliveryservice.deliveryLog.application.service.DeliveryLogService;
import com.sparta.deliveryservice.deliveryLog.presentation.dto.resqonse.DeliveryLogSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DeliveryLogController {

    private final DeliveryLogService deliveryLogService;

    @GetMapping("/deliveries/{deliveryId}/logs")
    public ResponseEntity<ApiResponse<PageResponse<DeliveryLogSearchResponse.DeliveryLogResponseDto>>> getDeliveryLogs(
            @PathVariable("deliveryId") UUID deliveryId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {

        Page<DeliveryLogSearchResponse.DeliveryLogResponseDto> logPage =
                deliveryLogService.getDeliveryLogs(deliveryId, pageable);

        PageResponse<DeliveryLogSearchResponse.DeliveryLogResponseDto> response =
                DeliveryLogSearchResponse.of(logPage);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}