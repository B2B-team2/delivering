package com.sparta.hubservice.inventory.presentation.dto;

import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class WarehouseInventoryResponse {

    private final UUID inventoryId;
    private final UUID warehouseId;
    private final UUID productOptionId;
    private final int quantity;
    private final int reservedQuantity;
    private final int availableQuantity;
    private final int safetyStock;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static WarehouseInventoryResponse from(WarehouseInventoryDto dto) {
        return WarehouseInventoryResponse.builder()
                .inventoryId(dto.getInventoryId())
                .warehouseId(dto.getWarehouseId())
                .productOptionId(dto.getProductOptionId())
                .quantity(dto.getQuantity())
                .reservedQuantity(dto.getReservedQuantity())
                .availableQuantity(dto.getAvailableQuantity())
                .safetyStock(dto.getSafetyStock())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
