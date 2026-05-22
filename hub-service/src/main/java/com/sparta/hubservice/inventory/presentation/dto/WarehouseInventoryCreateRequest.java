package com.sparta.hubservice.inventory.presentation.dto;

import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryCreateCommand;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class WarehouseInventoryCreateRequest {

    @NotNull
    private UUID warehouseId;

    @NotNull
    private UUID productOptionId;

    @Min(0) // 재고 수량은 음수가 될 수 없음
    private int quantity;

    @Min(0)
    private int safetyStock;

    public WarehouseInventoryCreateCommand toCommand() {
        return WarehouseInventoryCreateCommand.builder()
                .warehouseId(warehouseId)
                .productOptionId(productOptionId)
                .quantity(quantity)
                .safetyStock(safetyStock)
                .build();
    }
}
