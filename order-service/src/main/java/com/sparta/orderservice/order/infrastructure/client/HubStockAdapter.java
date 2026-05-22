package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.orderservice.order.application.port.HubStockPort;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.infrastructure.client.dto.InventoryBulkRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.InventoryItem;
import com.sparta.orderservice.order.infrastructure.client.dto.StockCancelRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.StockPartialCancelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HubStockAdapter implements HubStockPort {

    private final HubClient hubClient;

    // CompanyOrder별로 각각 재고 예약 호출
    @Override
    public void reserveStock(Order order) {
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
    }

    @Override
    public void cancelStock(UUID orderId) {
        hubClient.cancelStock(new StockCancelRequest(orderId));
    }

    @Override
    public void cancelCompanyStock(UUID companyOrderId) {
        hubClient.cancelCompanyStock(new StockPartialCancelRequest(companyOrderId));
    }

    @Override
    public void deductStock(CompanyOrder companyOrder) {
        List<InventoryItem> items = companyOrder.getOrderItems().stream()
                .map(item -> new InventoryItem(item.getProductOptionId(), item.getQuantity()))
                .toList();
        hubClient.deductStock(new InventoryBulkRequest(
                companyOrder.getOrder().getOrderId(),
                null,   // deduct는 companyOrderId 불필요
                items
        ));
    }
}
