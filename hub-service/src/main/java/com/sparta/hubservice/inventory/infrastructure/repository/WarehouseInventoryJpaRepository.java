package com.sparta.hubservice.inventory.infrastructure.repository;

import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseInventoryJpaRepository extends JpaRepository<WarehouseInventory, UUID> {

    Optional<WarehouseInventory> findByInventoryIdAndDeletedAtIsNull(UUID inventoryId);

    Optional<WarehouseInventory> findByWarehouseIdAndProductOptionIdAndDeletedAtIsNull(UUID warehouseId, UUID productOptionId);

    List<WarehouseInventory> findByWarehouseIdAndDeletedAtIsNull(UUID warehouseId);

    List<WarehouseInventory> findByProductOptionIdAndDeletedAtIsNull(UUID productOptionId);
}
