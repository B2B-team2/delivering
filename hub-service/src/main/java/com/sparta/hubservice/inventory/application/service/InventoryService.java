package com.sparta.hubservice.inventory.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.global.exception.StockValidationException;
import com.sparta.hubservice.inventory.application.dto.InventoryHistoryPageDto;
import com.sparta.hubservice.inventory.application.dto.InventoryItemCommand;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryAdjustCommand;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryAdjustDto;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryCreateCommand;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryDto;
import com.sparta.hubservice.inventory.domain.core.InventoryChangeType;
import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import com.sparta.hubservice.inventory.domain.repository.InventoryHistoryRepository;
import com.sparta.hubservice.inventory.domain.repository.WarehouseInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public WarehouseInventoryAdjustDto adjustInventory(UUID inventoryId, WarehouseInventoryAdjustCommand command) {
        WarehouseInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));

        int previousQuantity = inventory.getQuantity();

        if (previousQuantity + command.getChangeQuantity() < 0) {
            throw new StockValidationException("changeQuantity", "차감하려는 수량이 가용 재고보다 많습니다.");
        }

        inventory.adjust(command.getChangeQuantity());

        if (command.getSafetyStock() != null) {
            inventory.updateSafetyStock(command.getSafetyStock());
        }

        InventoryHistory history = historyRepository.save(InventoryHistory.builder()
                .inventoryId(inventoryId)
                .changeQuantity(command.getChangeQuantity())
                .changeType(parseChangeType(command.getChangeType()))
                .reason(command.getReason())
                .build());

        return WarehouseInventoryAdjustDto.from(previousQuantity, command.getChangeQuantity(), inventory, history);
    }

    public InventoryHistoryPageDto getInventoryHistories(UUID inventoryId, String changeType,
                                                         LocalDate startDate, LocalDate endDate,
                                                         Pageable pageable) {
        InventoryChangeType parsedChangeType = changeType != null ? parseChangeType(changeType) : null;
        WarehouseInventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(23, 59, 59) : null;

        Page<InventoryHistory> historyPage = historyRepository.findHistories(
                inventoryId, parsedChangeType, startDateTime, endDateTime, pageable);

        return new InventoryHistoryPageDto(inventory, historyPage);
    }

    @Transactional
    public void reserveStock(UUID orderId, List<InventoryItemCommand> items) {
        for (var item : items) {
            WarehouseInventory inventory = inventoryRepository.findById(item.getInventoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
            inventory.reserve(item.getQuantity());
            historyRepository.save(InventoryHistory.builder()
                    .inventoryId(item.getInventoryId())
                    .orderId(orderId)
                    .changeQuantity(item.getQuantity())
                    .changeType(InventoryChangeType.RESERVED)
                    .build());
        }
    }

    @Transactional
    public void cancelReservation(UUID orderId) {
        List<InventoryHistory> reservations = historyRepository.findByOrderIdAndChangeType(orderId, InventoryChangeType.RESERVED);
        if (reservations.isEmpty()) {
            throw new BusinessException(ErrorCode.INVENTORY_NOT_FOUND);
        }
        for (InventoryHistory reservation : reservations) {
            WarehouseInventory inventory = inventoryRepository.findById(reservation.getInventoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
            inventory.cancelReservation(reservation.getChangeQuantity());
            historyRepository.save(InventoryHistory.builder()
                    .inventoryId(reservation.getInventoryId())
                    .orderId(orderId)
                    .changeQuantity(-reservation.getChangeQuantity())
                    .changeType(InventoryChangeType.CANCELLED)
                    .build());
        }
    }

    @Transactional
    public void deductStock(UUID orderId, List<InventoryItemCommand> items) {
        for (var item : items) {
            WarehouseInventory inventory = inventoryRepository.findById(item.getInventoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
            inventory.deduct(item.getQuantity());
            historyRepository.save(InventoryHistory.builder()
                    .inventoryId(item.getInventoryId())
                    .orderId(orderId)
                    .changeQuantity(-item.getQuantity())
                    .changeType(InventoryChangeType.OUTBOUND)
                    .build());
        }
    }

    @Transactional
    public void returnStock(UUID orderId, List<InventoryItemCommand> items) {
        for (var item : items) {
            WarehouseInventory inventory = inventoryRepository.findById(item.getInventoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVENTORY_NOT_FOUND));
            inventory.returnStock(item.getQuantity());
            historyRepository.save(InventoryHistory.builder()
                    .inventoryId(item.getInventoryId())
                    .orderId(orderId)
                    .changeQuantity(item.getQuantity())
                    .changeType(InventoryChangeType.RETURNED)
                    .build());
        }
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
