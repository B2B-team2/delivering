package com.sparta.hubservice.inventory.domain.repository;

import com.sparta.hubservice.inventory.domain.core.InventoryChangeType;
import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface InventoryHistoryRepository {

    InventoryHistory save(InventoryHistory history);

    List<InventoryHistory> findByInventoryId(UUID inventoryId);

    Page<InventoryHistory> findHistories(UUID inventoryId, InventoryChangeType changeType,
                                         LocalDateTime startDateTime, LocalDateTime endDateTime,
                                         Pageable pageable);

    List<InventoryHistory> findByOrderIdAndChangeType(UUID orderId, InventoryChangeType changeType);

    List<InventoryHistory> findByCompanyOrderIdAndChangeType(UUID companyOrderId, InventoryChangeType changeType);
}
