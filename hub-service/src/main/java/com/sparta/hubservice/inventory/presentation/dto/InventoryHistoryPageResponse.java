package com.sparta.hubservice.inventory.presentation.dto;

import com.sparta.hubservice.inventory.application.dto.InventoryHistoryItemDto;
import com.sparta.hubservice.inventory.application.dto.InventoryHistoryPageDto;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
public class InventoryHistoryPageResponse {

    private final UUID inventoryId;
    private final UUID productOptionId;
    private final String productName;
    private final String optionsName;
    private final List<HistoryItem> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final String sort;

    public static InventoryHistoryPageResponse from(InventoryHistoryPageDto dto) {
        return new InventoryHistoryPageResponse(dto);
    }

    private InventoryHistoryPageResponse(InventoryHistoryPageDto dto) {
        this.inventoryId = dto.getInventoryId();
        this.productOptionId = dto.getProductOptionId();
        this.productName = dto.getProductName();
        this.optionsName = dto.getOptionsName();
        this.content = dto.getContent().stream().map(HistoryItem::from).toList();
        this.page = dto.getPage();
        this.size = dto.getSize();
        this.totalElements = dto.getTotalElements();
        this.totalPages = dto.getTotalPages();
        this.sort = dto.getSort();
    }

    @Getter
    public static class HistoryItem {
        private final UUID historyId;
        private final int changeQuantity;
        private final String changeType;
        private final String reason;
        private final LocalDateTime createdAt;
        private final String createdBy;

        private HistoryItem(InventoryHistoryItemDto dto) {
            this.historyId = dto.getHistoryId();
            this.changeQuantity = dto.getChangeQuantity();
            this.changeType = dto.getChangeType();
            this.reason = dto.getReason();
            this.createdAt = dto.getCreatedAt();
            this.createdBy = dto.getCreatedBy();
        }

        public static HistoryItem from(InventoryHistoryItemDto dto) {
            return new HistoryItem(dto);
        }
    }
}
