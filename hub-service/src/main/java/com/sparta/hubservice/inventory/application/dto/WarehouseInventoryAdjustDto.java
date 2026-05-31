package com.sparta.hubservice.inventory.application.dto;

import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class WarehouseInventoryAdjustDto {

    private final UUID inventoryId;
    private final UUID productOptionId;
    private final int previousQuantity;
    private final int changeQuantity;
    private final int currentQuantity;
    private final int reservedQuantity;
    private final int availableQuantity;
    private final int safetyStock;
    private final Long version;
    private final UUID historyId;
    private final LocalDateTime updatedAt;
    private final UUID updatedBy;

    public static WarehouseInventoryAdjustDto from(int previousQuantity, int changeQuantity,
                                                    WarehouseInventory inventory,
                                                    InventoryHistory history) {
        return WarehouseInventoryAdjustDto.builder()
                .inventoryId(inventory.getInventoryId())
                .productOptionId(inventory.getProductOptionId())
                .previousQuantity(previousQuantity)
                .changeQuantity(changeQuantity)
                .currentQuantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .safetyStock(inventory.getSafetyStock())
                .version(inventory.getVersion())
                .historyId(history.getHistoryId())
                .updatedAt(inventory.getUpdatedAt())
                .updatedBy(inventory.getUpdatedBy())
                .build();
    }
}
