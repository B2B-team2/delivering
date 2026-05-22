package com.sparta.hubservice.inventory.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseInventoryAdjustCommand {

    private final int changeQuantity;   // 양수: 증가, 음수: 감소
    private final String changeType;    // INBOUND, OUTBOUND, ADJUSTED, RETURNED 등
    private final String reason;        // 자유 입력 사유 (저장되지 않음)
    private final Integer safetyStock;
}
