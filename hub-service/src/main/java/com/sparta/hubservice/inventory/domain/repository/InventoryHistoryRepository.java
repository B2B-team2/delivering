package com.sparta.hubservice.inventory.domain.repository;

import com.sparta.hubservice.inventory.domain.core.InventoryHistory;

import java.util.List;
import java.util.UUID;

public interface InventoryHistoryRepository {

    InventoryHistory save(InventoryHistory history);

    List<InventoryHistory> findByInventoryId(UUID inventoryId);
}
