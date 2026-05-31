package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.orderservice.order.infrastructure.client.dto.InventoryBulkRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.StockCancelRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.StockPartialCancelRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "hub-service")
public interface HubClient {

    // 재고 예약: 주문 생성 시 호출, 실패 시 주문 생성 중단 (예외 전파)
    @PostMapping("/api/v1/internal/inventory/reserve")
    void reserveStock(@RequestBody InventoryBulkRequest request);

    // 재고 예약 전체 취소: 주문 전체 취소 시 호출 (orderId 기준)
    @PostMapping("/api/v1/internal/inventory/cancel")
    void cancelStock(@RequestBody StockCancelRequest request);

    // 재고 예약 부분 취소: CompanyOrder 단위 취소 시 호출 (companyOrderId 기준)
    @PostMapping("/api/v1/internal/inventory/cancel/company")
    void cancelCompanyStock(@RequestBody StockPartialCancelRequest request);

    // 반품 재고 복원 — 반품 도메인 담당 서비스 확정 후 연동 여부 결정
//    @PostMapping("/api/v1/internal/inventory/return")
//    void returnStock(@RequestBody InventoryBulkRequest request);
}
