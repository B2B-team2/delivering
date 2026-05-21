package com.sparta.hubservice.inventory.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryAdjustCommand;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryCreateCommand;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryDto;
import com.sparta.hubservice.inventory.domain.core.InventoryChangeType;
import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import com.sparta.hubservice.inventory.domain.repository.InventoryHistoryRepository;
import com.sparta.hubservice.inventory.domain.repository.WarehouseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryService {

    private final WarehouseInventoryRepository inventoryRepository;
    private final InventoryHistoryRepository historyRepository;

    @Transactional
    public WarehouseInventoryDto createInventory(WarehouseInventoryCreateCommand command) {
        inventoryRepository.findByWarehouseIdAndProductOptionId(command.getWarehouseId(), command.getProductOptionId())
                .ifPresent(i -> { throw new BusinessException(ErrorCode.DUPLICATE_INVENTORY); });

        WarehouseInventory inventory = WarehouseInventory.builder()
                .warehouseId(command.getWarehouseId())
                .productOptionId(command.getProductOptionId())
                .quantity(command.getQuantity())
                .safetyStock(command.getSafetyStock())
                .build();

        WarehouseInventory saved = inventoryRepository.save(inventory);

        if (command.getQuantity() > 0) {
            historyRepository.save(InventoryHistory.builder()
                    .inventoryId(saved.getInventoryId())
                    .changeQuantity(command.getQuantity())
                    .changeType(InventoryChangeType.INBOUND)
                    .build());
        }

        return WarehouseInventoryDto.from(saved);
    }

    public WarehouseInventoryDto getInventory(UUID inventoryId) {
        return WarehouseInventoryDto.from(inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND)));
    }

    public List<WarehouseInventoryDto> getInventoriesByWarehouse(UUID warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId).stream()
                .map(WarehouseInventoryDto::from)
                .toList();
    }

    @Transactional
    public WarehouseInventoryDto adjustInventory(UUID inventoryId, WarehouseInventoryAdjustCommand command) {
        WarehouseInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));

        if (inventory.getQuantity() + command.getAdjustQuantity() < 0) {
            throw new BusinessException(ErrorCode.INVALID_STOCK_OPERATION);
        }

        inventory.adjust(command.getAdjustQuantity());

        if (command.getSafetyStock() != null) {
            inventory.updateSafetyStock(command.getSafetyStock());
        }

        historyRepository.save(InventoryHistory.builder()
                .inventoryId(inventoryId)
                .changeQuantity(command.getAdjustQuantity())
                .changeType(parseChangeType(command.getReason()))
                .build());

        return WarehouseInventoryDto.from(inventory);
    }

    @Transactional
    public void deleteInventory(UUID inventoryId, String deletedBy) {
        WarehouseInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
        inventory.softDelete(deletedBy);
        inventoryRepository.save(inventory);
    }

    private InventoryChangeType parseChangeType(String reason) {
        if (reason == null) return InventoryChangeType.ADJUSTED;
        try {
            return InventoryChangeType.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException e) {
            return InventoryChangeType.ADJUSTED;
        }
    }
}
