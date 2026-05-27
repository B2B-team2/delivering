package com.sparta.operationsservice.claim.infrastructure.client;

import com.sparta.operationsservice.claim.application.port.HubPort;
import com.sparta.operationsservice.claim.infrastructure.client.dto.InventoryBulkRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HubAdapter implements HubPort {

    private final HubClient hubClient;

    @Override
    public void returnStock(UUID orderId, List<InventoryItem> items) {
        InventoryBulkRequest request = InventoryBulkRequest.builder()
                .orderId(orderId)
                .items(items.stream()
                        .map(item -> InventoryBulkRequest.InventoryItemRequest.builder()
                                .productOptionId(item.productOptionId())
                                .quantity(item.quantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        
        hubClient.returnStock(request);
    }
}
