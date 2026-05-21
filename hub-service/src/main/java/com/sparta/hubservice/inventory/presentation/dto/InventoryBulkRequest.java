package com.sparta.hubservice.inventory.presentation.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class InventoryBulkRequest {

    @NotNull
    private UUID orderId;

    @Valid
    @NotEmpty
    private List<InventoryItemRequest> items;

    @Getter
    public static class InventoryItemRequest {

        @NotNull
        private UUID inventoryId;

        @Positive
        private int quantity;
    }
}
