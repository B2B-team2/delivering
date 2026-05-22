package com.sparta.hubservice.inventory.application.dto;

import com.sparta.hubservice.inventory.domain.core.InventoryHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class InventoryHistoryItemDto {

    private final UUID historyId;
    private final int changeQuantity;
    private final String changeType;
    private final String reason;
    private final LocalDateTime createdAt;
    private final String createdBy;

    public static InventoryHistoryItemDto from(InventoryHistory history) {
        return InventoryHistoryItemDto.builder()
                .historyId(history.getHistoryId())
                .changeQuantity(history.getChangeQuantity())
                .changeType(history.getChangeType().name())
                .reason(history.getReason())
                .createdAt(history.getCreatedAt())
                .createdBy(history.getCreatedBy())
                .build();
    }
}
