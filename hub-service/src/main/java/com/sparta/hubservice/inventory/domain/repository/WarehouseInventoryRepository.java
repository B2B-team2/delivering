package com.sparta.hubservice.inventory.domain.repository;

import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseInventoryRepository {

    WarehouseInventory save(WarehouseInventory inventory);

    Optional<WarehouseInventory> findById(UUID inventoryId);

    Optional<WarehouseInventory> findByWarehouseIdAndProductOptionId(UUID warehouseId, UUID productOptionId);

    List<WarehouseInventory> findByWarehouseId(UUID warehouseId);

    void delete(WarehouseInventory inventory, String deletedBy);
}
