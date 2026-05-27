package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.port.HubStockPort;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.infrastructure.client.dto.InventoryBulkRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.InventoryItem;
import com.sparta.orderservice.order.infrastructure.client.dto.StockCancelRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.StockPartialCancelRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubStockAdapter implements HubStockPort {

    private final HubClient hubClient;

    // CompanyOrder별로 각각 재고 예약 호출
    @Override
    public void reserveStock(Order order) {
        try {
            order.getCompanyOrders().forEach(companyOrder -> {
                List<InventoryItem> items = companyOrder.getOrderItems().stream()
                        .map(item -> new InventoryItem(item.getProductOptionId(), item.getQuantity()))
                        .toList();
                hubClient.reserveStock(new InventoryBulkRequest(
                        order.getOrderId(),
                        companyOrder.getCompanyOrderId(),
                        items
                ));
            });
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("reserveStock", e);
        }
    }

    // 주문 전체 재고 예약 취소 (보상 트랜잭션)
    @Override
    public void cancelStock(UUID orderId) {
        try {
            hubClient.cancelStock(new StockCancelRequest(orderId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("cancelStock", e);
        }
    }

    // 업체 주문 부분 재고 예약 취소 (보상 트랜잭션)
    @Override
    public void cancelCompanyStock(UUID companyOrderId) {
        try {
            hubClient.cancelCompanyStock(new StockPartialCancelRequest(companyOrderId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("cancelCompanyStock", e);
        }
    }

    // cancelCompanyOrder Saga 보상 전용: 특정 CompanyOrder의 재고 재예약
    @Override
    public void reserveCompanyStock(CompanyOrder companyOrder) {
        try {
            List<InventoryItem> items = companyOrder.getOrderItems().stream()
                    .map(item -> new InventoryItem(item.getProductOptionId(), item.getQuantity()))
                    .toList();
            hubClient.reserveStock(new InventoryBulkRequest(
                    companyOrder.getOrder().getOrderId(),
                    companyOrder.getCompanyOrderId(),
                    items
            ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("reserveCompanyStock", e);
        }
    }

    private RuntimeException handleUnexpectedException(String operation, Exception e) {
        log.error("[Hub] Unexpected error [{}]", operation, e);
        return new BusinessException(OrderErrorCode.HUB_SERVICE_UNAVAILABLE);
    }
}
