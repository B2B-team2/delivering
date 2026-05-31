package com.sparta.operationsservice.claim.infrastructure.client;

import com.sparta.operationsservice.claim.application.port.HubPort;
import com.sparta.operationsservice.claim.infrastructure.client.dto.InventoryBulkRequest;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubAdapter implements HubPort {

    private final HubClient hubClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public void returnStock(UUID orderId, List<InventoryItem> items) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker");
        
        log.info("[Resilience] Manual Call Executed - State: {}", circuitBreaker.getState());
        
        circuitBreaker.executeRunnable(() -> {
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
        });
    }

    @Override
    public void deductStock(UUID orderId, List<InventoryItem> items) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker");

        log.info("[Resilience] Manual Call Executed - State: {}", circuitBreaker.getState());

        circuitBreaker.executeRunnable(() -> {
            InventoryBulkRequest request = InventoryBulkRequest.builder()
                    .orderId(orderId)
                    .items(items.stream()
                            .map(item -> InventoryBulkRequest.InventoryItemRequest.builder()
                                    .productOptionId(item.productOptionId())
                                    .quantity(item.quantity())
                                    .build())
                            .collect(Collectors.toList()))
                    .build();
            hubClient.deductStock(request);
        });
    }
}
