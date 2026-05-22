package com.sparta.hubservice.inventory.presentation.dto;

import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryAdjustCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WarehouseInventoryAdjustRequest {

    @NotNull
    private int adjustQuantity;

    private Integer safetyStock;

    private String reason;

    public WarehouseInventoryAdjustCommand toCommand() {
        return WarehouseInventoryAdjustCommand.builder()
                .adjustQuantity(adjustQuantity)
                .safetyStock(safetyStock)
                .reason(reason)
                .build();
    }
}
