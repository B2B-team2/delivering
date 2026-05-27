package com.sparta.orderservice.order.presentation.controller;

import com.sparta.orderservice.order.application.service.CompanyOrderStatusService;
import com.sparta.orderservice.order.application.service.OrderQueryService;
import com.sparta.orderservice.order.presentation.dto.ClaimCancelResponse;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderDeliveredRequest;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderDetailsResponse;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// 서비스 간 내부 호출 전용 컨트롤러 — plain DTO 반환 (ApiResponse 래핑 없음)
@RestController
@RequestMapping("/api/v1/internal/orders")
@RequiredArgsConstructor
public class InternalOrderController {

    private final CompanyOrderStatusService companyOrderStatusService;
    private final OrderQueryService orderQueryService;

    /**
     * 업체 주문 상세 정보 조회 (상위 orderId + 상품 목록)
     */
    @GetMapping("/company/{companyOrderId}/details")
    public CompanyOrderDetailsResponse getCompanyOrderDetails(@PathVariable UUID companyOrderId) {
        return orderQueryService.getCompanyOrderDetails(companyOrderId);
    }

    /**
     * 업체 주문 수령 완료 처리 (SHIPPED → DELIVERED)
     * 모든 CompanyOrder가 DELIVERED이면 Order → COMPLETED 자동 전환
     */
    @PatchMapping("/company/{companyOrderId}/delivered")
    public void deliverCompanyOrder(
            @PathVariable UUID companyOrderId,
            @RequestHeader("X-User-Id") UUID requesterId,
            @RequestBody CompanyOrderDeliveredRequest request) {  // body 수신 유지 (delivery-service 호출 규격)
        companyOrderStatusService.confirmDelivery(companyOrderId, requesterId);
    }

    /**
     * 클레임 처리 서브 주문 취소 (SHIPPED/DELIVERED → CANCELLED)
     * operations-service 내부 전용 — 상태 변경만, 재고/배송 보상 없음
     */
    @PatchMapping("/company/{companyOrderId}/claim-cancel")
    public ClaimCancelResponse cancelCompanyOrderByClaim(
            @PathVariable UUID companyOrderId,
            @RequestHeader("X-User-Id") UUID requesterId) {
        return ClaimCancelResponse.from(companyOrderStatusService.cancelCompanyOrderByClaim(companyOrderId, requesterId));
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
