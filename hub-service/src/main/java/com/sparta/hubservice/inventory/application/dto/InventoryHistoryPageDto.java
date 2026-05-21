package com.sparta.hubservice.inventory.application.dto;

import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@Getter
public class InventoryHistoryPageDto {

    private final UUID inventoryId;
    private final UUID productOptionId;
    private final String productName;   // product 서비스 Feign 연동 필요, 현재 null
    private final String optionsName;   // product 서비스 Feign 연동 필요, 현재 null
    private final List<InventoryHistoryItemDto> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final String sort;

    public InventoryHistoryPageDto(WarehouseInventory inventory, Page<InventoryHistory> historyPage) {
        this.inventoryId = inventory.getInventoryId();
        this.productOptionId = inventory.getProductOptionId();
        this.productName = null;
        this.optionsName = null;
        this.content = historyPage.getContent().stream()
                .map(InventoryHistoryItemDto::from)
                .toList();
        this.page = historyPage.getNumber();
        this.size = historyPage.getSize();
        this.totalElements = historyPage.getTotalElements();
        this.totalPages = historyPage.getTotalPages();
        this.sort = historyPage.getSort().toString().replace(": ", ",");
    }
}
