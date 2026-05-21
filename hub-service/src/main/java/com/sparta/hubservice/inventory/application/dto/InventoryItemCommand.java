package com.sparta.hubservice.inventory.application.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class InventoryItemCommand {

    private final UUID inventoryId;
    private final int quantity;
}
