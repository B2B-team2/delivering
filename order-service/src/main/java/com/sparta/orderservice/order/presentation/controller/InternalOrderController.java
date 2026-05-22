package com.sparta.orderservice.order.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.orderservice.order.application.dto.CompanyOrderDeliveredResult;
import com.sparta.orderservice.order.application.service.OrderService;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderDeliveredResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 서비스 간 내부 호출 전용 컨트롤러
@RestController
@RequestMapping("/api/v1/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final OrderService orderService;

    /**
     * 업체 주문 수령 완료 처리 (SHIPPED → DELIVERED)
     * 모든 CompanyOrder가 DELIVERED이면 Order → COMPLETED 자동 전환
     */
    @PatchMapping("/company/{companyOrderId}/delivered")
    public ResponseEntity<ApiResponse<CompanyOrderDeliveredResponse>> deliverCompanyOrder(
            @PathVariable UUID companyOrderId
    ) {
        CompanyOrderDeliveredResult result = orderService.confirmDelivery(companyOrderId);
        return ResponseEntity.ok(ApiResponse.success(CompanyOrderDeliveredResponse.from(result)));
    }
}
