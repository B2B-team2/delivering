package com.sparta.hubservice.inventory.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class WarehouseInventoryCreateCommand {

    private final UUID warehouseId;
    private final UUID productOptionId;
    private final int quantity;
    private final int safetyStock;
}
