package com.sparta.hubservice.inventory.infrastructure.repository;

import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import com.sparta.hubservice.inventory.domain.repository.InventoryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class InventoryHistoryRepositoryImpl implements InventoryHistoryRepository {

    private final InventoryHistoryJpaRepository inventoryHistoryJpaRepository;

    @Override
    public InventoryHistory save(InventoryHistory history) {
        return inventoryHistoryJpaRepository.save(history);
    }

    @Override
    public List<InventoryHistory> findByInventoryId(UUID inventoryId) {
        return inventoryHistoryJpaRepository.findByInventoryId(inventoryId);
    }
}
