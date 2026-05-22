package com.sparta.hubservice.inventory.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseInventoryAdjustCommand {

    private final int adjustQuantity;   // 양수: 증가, 음수: 감소
    private final Integer safetyStock;
    private final String reason;        // 조정 사유 (ADJUSTED, INBOUND, RETURNED 등)
}
