package com.sparta.operationsservice.claim.application.port;

import java.util.List;
import java.util.UUID;

public interface HubPort {
    void returnStock(UUID orderId, List<InventoryItem> items);

    record InventoryItem(
            UUID productOptionId,
            int quantity
    ) {}
}
