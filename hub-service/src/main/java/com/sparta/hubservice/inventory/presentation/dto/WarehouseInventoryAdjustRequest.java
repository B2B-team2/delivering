package com.sparta.hubservice.inventory.presentation.dto;

import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryAdjustCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class WarehouseInventoryAdjustRequest {

    @NotNull
    private Integer changeQuantity;

    @NotNull
    private String changeType;

    private String reason;

    private Integer safetyStock;

    public WarehouseInventoryAdjustCommand toCommand() {
        return WarehouseInventoryAdjustCommand.builder()
                .changeQuantity(changeQuantity)
                .changeType(changeType)
                .safetyStock(safetyStock)
                .reason(reason)
                .build();
    }
}
