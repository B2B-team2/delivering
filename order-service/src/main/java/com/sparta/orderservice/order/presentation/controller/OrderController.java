package com.sparta.orderservice.order.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.orderservice.order.application.service.OrderCommandService;
import com.sparta.orderservice.order.application.service.OrderQueryService;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderResponse;
import com.sparta.orderservice.order.presentation.dto.OrderCreateRequest;
import com.sparta.orderservice.order.presentation.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    // 주문 생성
    // MASTER, COMPANY_MANAGER만 허용 (서비스 레이어에서 검증)
    // receiverCompanyId: COMPANY_MANAGER → X-Company-Id 헤더, MASTER → 요청 body
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @RequestBody @Valid OrderCreateRequest request,
        @RequestHeader("X-User-Id") UUID requesterId,
        @RequestHeader(value = "X-Company-Id", required = false) UUID companyIdFromHeader
    ) {
        UUID receiverCompanyId = companyIdFromHeader != null ? companyIdFromHeader : request.receiverCompanyId();
        OrderResponse response = OrderResponse.from(
                orderCommandService.createOrder(request.toCommand(receiverCompanyId), requesterId));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    // 주문 목록 조회 (페이징)
    // MASTER → 전체 / COMPANY_MANAGER → 자기 회사 / 그 외 → 403 (서비스 레이어에서 처리)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<OrderResponse> response = new PageResponse<>(
                orderQueryService.getOrders(pageable).map(OrderResponse::from));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주문 단건 상세 조회
    // MASTER → 전체 / COMPANY_MANAGER → 자기 회사 관련 주문만 (서비스 레이어에서 검증)
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID orderId) {
        OrderResponse response = OrderResponse.from(orderQueryService.getOrder(orderId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주문 취소
    // MASTER → 전체 / COMPANY_MANAGER → 자기 회사가 수령업체인 주문만 (서비스 레이어에서 검증)
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        orderCommandService.cancelOrder(orderId, requesterId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 서브 주문 상세 조회
    // MASTER → 전체 / COMPANY_MANAGER → 자기 회사가 공급업체인 CompanyOrder만 (서비스 레이어에서 검증)
    @GetMapping("/company/{companyOrderId}")
    public ResponseEntity<ApiResponse<CompanyOrderResponse>> getCompanyOrder(
        @PathVariable UUID companyOrderId
    ) {
        CompanyOrderResponse response = CompanyOrderResponse.from(orderQueryService.getCompanyOrder(companyOrderId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 서브 주문 부분 취소
    // MASTER → 전체 / COMPANY_MANAGER → 자기 회사가 공급업체인 CompanyOrder만 (서비스 레이어에서 검증)
    @PatchMapping("/company/{companyOrderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelCompanyOrder(
        @PathVariable UUID companyOrderId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        orderCommandService.cancelCompanyOrder(companyOrderId, requesterId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
