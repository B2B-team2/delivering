package com.sparta.hubservice.inventory.infrastructure.repository;

import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import com.sparta.hubservice.inventory.domain.repository.WarehouseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WarehouseInventoryRepositoryImpl implements WarehouseInventoryRepository {

    private final WarehouseInventoryJpaRepository warehouseInventoryJpaRepository;

    @Override
    public WarehouseInventory save(WarehouseInventory inventory) {
        return warehouseInventoryJpaRepository.save(inventory);
    }

    @Override
    public Optional<WarehouseInventory> findById(UUID inventoryId) {
        return warehouseInventoryJpaRepository.findByInventoryIdAndDeletedAtIsNull(inventoryId);
    }

    @Override
    public Optional<WarehouseInventory> findByWarehouseIdAndProductOptionId(UUID warehouseId, UUID productOptionId) {
        return warehouseInventoryJpaRepository.findByWarehouseIdAndProductOptionIdAndDeletedAtIsNull(warehouseId, productOptionId);
    }

    @Override
    public List<WarehouseInventory> findByWarehouseId(UUID warehouseId) {
        return warehouseInventoryJpaRepository.findByWarehouseIdAndDeletedAtIsNull(warehouseId);
    }

    @Override
    public List<WarehouseInventory> findByProductOptionId(UUID productOptionId) {
        return warehouseInventoryJpaRepository.findByProductOptionIdAndDeletedAtIsNull(productOptionId);
    }

    @Override
    public void delete(WarehouseInventory inventory, String deletedBy) {
        inventory.softDelete(deletedBy);
        warehouseInventoryJpaRepository.save(inventory);
    }
}
