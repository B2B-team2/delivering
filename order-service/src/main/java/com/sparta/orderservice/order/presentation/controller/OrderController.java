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
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @RequestBody @Valid OrderCreateRequest request,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(OrderResponse.from(orderCommandService.createOrder(request.toCommand(), requesterId))));
    }

    // 전체 주문 조회 (페이징)
    // TODO: 권한별 필터링 (마스터/허브관리자 → 전체, 업체 담당자 → 자기 회사 주문만)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getOrders(
        @RequestHeader("X-User-Id") UUID requesterId,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<OrderResponse> response = new PageResponse<>(
                orderQueryService.getOrders(requesterId, pageable).map(OrderResponse::from));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 주문 단건 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(OrderResponse.from(orderQueryService.getOrder(orderId))));
    }

    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        orderCommandService.cancelOrder(orderId, requesterId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 서브 주문 상세 조회
    @GetMapping("/company/{companyOrderId}")
    public ResponseEntity<ApiResponse<CompanyOrderResponse>> getCompanyOrder(
        @PathVariable UUID companyOrderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(CompanyOrderResponse.from(orderQueryService.getCompanyOrder(companyOrderId))));
    }

    // 서브 주문 부분 취소
    @PatchMapping("/company/{companyOrderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelCompanyOrder(
        @PathVariable UUID companyOrderId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        orderCommandService.cancelCompanyOrder(companyOrderId, requesterId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
