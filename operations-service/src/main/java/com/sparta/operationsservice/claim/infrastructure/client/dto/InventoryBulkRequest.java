package com.sparta.operationsservice.claim.infrastructure.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryBulkRequest {
    private UUID orderId;
    private List<InventoryItemRequest> items;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InventoryItemRequest {
        private UUID productOptionId;
        private int quantity;
    }
}
