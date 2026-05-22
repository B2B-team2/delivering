package com.sparta.hubservice.inventory.infrastructure.repository;

import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryHistoryJpaRepository extends JpaRepository<InventoryHistory, UUID> {

    List<InventoryHistory> findByInventoryId(UUID inventoryId);
}
