package com.sparta.orderservice.order.presentation.controller;

import com.sparta.orderservice.order.application.service.OrderService;
import com.sparta.orderservice.order.presentation.dto.OrderCreateRequest;
import com.sparta.orderservice.order.presentation.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    // 주문 생성
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
        @RequestBody OrderCreateRequest request,
        @RequestHeader("X-User-Id") UUID requesterId  // Gateway에서 JWT 파싱 후 헤더로 전달
    ) {
        return ResponseEntity.ok(orderService.createOrder(request, requesterId));
    }

    // 주문 단건 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    // 주문 취소
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
        @PathVariable UUID orderId,
        @RequestHeader("X-User-Id") UUID requesterId
    ) {
        orderService.cancelOrder(orderId, requesterId);
        return ResponseEntity.noContent().build();
    }

    // 출고 준비 (허브 관리자가 확인)
    @PutMapping("/company-orders/{companyOrderId}/prepare")
    public ResponseEntity<Void> prepareShipment(@PathVariable UUID companyOrderId) {
        orderService.prepareShipment(companyOrderId);
        return ResponseEntity.ok().build();
    }

    // 출고 완료 (재고 차감 + SHIPPED 상태 변경)
    @PutMapping("/company-orders/{companyOrderId}/ship")
    public ResponseEntity<Void> completeShipment(@PathVariable UUID companyOrderId) {
        orderService.completeShipment(companyOrderId);
        return ResponseEntity.ok().build();
    }
}
