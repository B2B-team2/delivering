package com.sparta.hubservice.inventory.infrastructure.repository;

import com.sparta.hubservice.inventory.domain.core.InventoryChangeType;
import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface InventoryHistoryJpaRepository extends JpaRepository<InventoryHistory, UUID>, JpaSpecificationExecutor<InventoryHistory> {

    List<InventoryHistory> findByInventoryId(UUID inventoryId);

    List<InventoryHistory> findByOrderIdAndChangeTypeAndDeletedAtIsNull(UUID orderId, InventoryChangeType changeType);

    List<InventoryHistory> findByCompanyOrderIdAndChangeTypeAndDeletedAtIsNull(UUID companyOrderId, InventoryChangeType changeType);
}
