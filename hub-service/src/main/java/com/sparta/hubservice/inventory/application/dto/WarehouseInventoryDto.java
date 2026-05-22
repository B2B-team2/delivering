package com.sparta.hubservice.inventory.application.dto;

import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class WarehouseInventoryDto {

    private final UUID inventoryId;
    private final UUID warehouseId;
    private final UUID productOptionId;
    private final int quantity;
    private final int reservedQuantity;
    private final int availableQuantity;
    private final int safetyStock;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static WarehouseInventoryDto from(WarehouseInventory inventory) {
        return WarehouseInventoryDto.builder()
                .inventoryId(inventory.getInventoryId())
                .warehouseId(inventory.getWarehouseId())
                .productOptionId(inventory.getProductOptionId())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .safetyStock(inventory.getSafetyStock())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
