package com.sparta.orderservice.order.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.orderservice.order.application.service.OrderService;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderResponse;
import com.sparta.orderservice.order.presentation.dto.OrderCreateRequest;
import com.sparta.orderservice.order.presentation.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @RequestBody @Valid OrderCreateRequest request,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(orderService.createOrder(request, requesterId)));
    }

    // 전체 주문 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrders(
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrders(requesterId)));
    }

    // 주문 단건 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrder(orderId)));
    }

    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        orderService.cancelOrder(orderId, requesterId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 서브 주문 상세 조회
    @GetMapping("/company/{companyOrderId}")
    public ResponseEntity<ApiResponse<CompanyOrderResponse>> getCompanyOrder(
        @PathVariable UUID companyOrderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getCompanyOrder(companyOrderId)));
    }

    // 서브 주문 부분 취소
    @PatchMapping("/company/{companyOrderId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelCompanyOrder(
        @PathVariable UUID companyOrderId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        orderService.cancelCompanyOrder(companyOrderId, requesterId);
        return ResponseEntity.ok(ApiResponse.success());
    }

//    // todo 출고 준비
//    @Put? Patch?
//    public ResponseEntity<Void> prepareShipment(@PathVariable UUID companyOrderId) {
//    }
//
//    // 출고 완료 (재고 차감 + SHIPPED 상태 변경)
//    @Put? Patch?
//    public ResponseEntity<Void> completeShipment(@PathVariable UUID companyOrderId) {
//        orderService.completeShipment(companyOrderId);
//        return ResponseEntity.ok().build();
//    }
}
