package com.sparta.hubservice.inventory.infrastructure.repository;

import com.sparta.hubservice.inventory.domain.core.InventoryChangeType;
import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import com.sparta.hubservice.inventory.domain.repository.InventoryHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Override
    public List<InventoryHistory> findByOrderIdAndChangeType(UUID orderId, InventoryChangeType changeType) {
        return inventoryHistoryJpaRepository.findByOrderIdAndChangeType(orderId, changeType);
    }

    @Override
    public List<InventoryHistory> findByCompanyOrderIdAndChangeType(UUID companyOrderId, InventoryChangeType changeType) {
        return inventoryHistoryJpaRepository.findByCompanyOrderIdAndChangeType(companyOrderId, changeType);
    }

    @Override
    public Page<InventoryHistory> findHistories(UUID inventoryId, InventoryChangeType changeType,
                                                LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                Pageable pageable) {
        Specification<InventoryHistory> spec = Specification
                .<InventoryHistory>where((root, query, cb) -> cb.equal(root.get("inventoryId"), inventoryId));

        if (changeType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("changeType"), changeType));
        }
        if (startDateTime != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
        }
        if (endDateTime != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
        }

        return inventoryHistoryJpaRepository.findAll(spec, pageable);
    }
}
