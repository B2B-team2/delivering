package com.sparta.orderservice.order.presentation.controller;

import com.sparta.orderservice.order.application.service.CompanyOrderStatusService;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderDeliveredResponse;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 서비스 간 내부 호출 전용 컨트롤러 — plain DTO 반환 (ApiResponse 래핑 없음)
@RestController
@RequestMapping("/api/v1/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final CompanyOrderStatusService companyOrderStatusService;

    /**
     * 업체 주문 수령 완료 처리 (SHIPPED → DELIVERED)
     * 모든 CompanyOrder가 DELIVERED이면 Order → COMPLETED 자동 전환
     */
    @PatchMapping("/company/{companyOrderId}/delivered")
    public CompanyOrderDeliveredResponse deliverCompanyOrder(@PathVariable UUID companyOrderId) {
        return CompanyOrderDeliveredResponse.from(companyOrderStatusService.confirmDelivery(companyOrderId));
    }

    /**
     * 출고 준비 확인 (ORDERED → PREPARING)
     */
    @PatchMapping("/company/{companyOrderId}/preparing")
    public CompanyOrderResponse prepareCompanyOrder(@PathVariable UUID companyOrderId) {
        return CompanyOrderResponse.from(companyOrderStatusService.prepareCompanyOrder(companyOrderId));
    }

    /**
     * 출고 완료 (PREPARING → SHIPPED)
     */
    @PatchMapping("/company/{companyOrderId}/shipped")
    public CompanyOrderResponse shipCompanyOrder(@PathVariable UUID companyOrderId) {
        return CompanyOrderResponse.from(companyOrderStatusService.shipCompanyOrder(companyOrderId));
    }
}
