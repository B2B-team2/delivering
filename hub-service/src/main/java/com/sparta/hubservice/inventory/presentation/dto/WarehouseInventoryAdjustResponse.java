package com.sparta.hubservice.inventory.presentation.dto;

import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryAdjustDto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class WarehouseInventoryAdjustResponse {

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

    public static WarehouseInventoryAdjustResponse from(WarehouseInventoryAdjustDto dto) {
        return WarehouseInventoryAdjustResponse.builder()
                .inventoryId(dto.getInventoryId())
                .productOptionId(dto.getProductOptionId())
                .previousQuantity(dto.getPreviousQuantity())
                .changeQuantity(dto.getChangeQuantity())
                .currentQuantity(dto.getCurrentQuantity())
                .reservedQuantity(dto.getReservedQuantity())
                .availableQuantity(dto.getAvailableQuantity())
                .safetyStock(dto.getSafetyStock())
                .version(dto.getVersion())
                .historyId(dto.getHistoryId())
                .updatedAt(dto.getUpdatedAt())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }
}
