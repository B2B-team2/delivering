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
import feign.FeignException;
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
        } catch (FeignException.Conflict e) {
            throw new BusinessException(OrderErrorCode.STOCK_INSUFFICIENT); // 409: 재고 부족
        } catch (FeignException e) {
            handleHubFeignException("reserveStock", e);
        } catch (Exception e) {
            handleHubUnexpectedException("reserveStock", e);
        }
    }

    // 주문 전체 재고 예약 취소 (보상 트랜잭션)
    @Override
    public void cancelStock(UUID orderId) {
        try {
            hubClient.cancelStock(new StockCancelRequest(orderId));
        } catch (FeignException e) {
            handleHubFeignException("cancelStock", e);
        } catch (Exception e) {
            handleHubUnexpectedException("cancelStock", e);
        }
    }

    // 업체 주문 부분 재고 예약 취소 (보상 트랜잭션)
    @Override
    public void cancelCompanyStock(UUID companyOrderId) {
        try {
            hubClient.cancelCompanyStock(new StockPartialCancelRequest(companyOrderId));
        } catch (FeignException e) {
            handleHubFeignException("cancelCompanyStock", e);
        } catch (Exception e) {
            handleHubUnexpectedException("cancelCompanyStock", e);
        }
    }

    // 출고 완료 시 실재고 차감
    @Override
    public void deductStock(CompanyOrder companyOrder) {
        try {
            List<InventoryItem> items = companyOrder.getOrderItems().stream()
                    .map(item -> new InventoryItem(item.getProductOptionId(), item.getQuantity()))
                    .toList();
            hubClient.deductStock(new InventoryBulkRequest(
                    companyOrder.getOrder().getOrderId(),
                    null,   // deduct는 companyOrderId 불필요
                    items
            ));
        } catch (FeignException e) {
            handleHubFeignException("deductStock", e);
        } catch (Exception e) {
            handleHubUnexpectedException("deductStock", e);
        }
    }

    private void handleHubFeignException(String operation, FeignException e) {
        log.error("Hub service error [{}]: status={}", operation, e.status());
        throw new BusinessException(OrderErrorCode.HUB_SERVICE_UNAVAILABLE);
    }

    private void handleHubUnexpectedException(String operation, Exception e) {
        log.error("Unexpected error [{}]", operation, e);
        throw new BusinessException(OrderErrorCode.HUB_SERVICE_UNAVAILABLE);
    }
}
