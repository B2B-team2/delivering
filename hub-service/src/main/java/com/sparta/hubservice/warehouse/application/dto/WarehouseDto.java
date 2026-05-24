package com.sparta.hubservice.warehouse.application.dto;

import com.sparta.hubservice.warehouse.domain.core.Warehouse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class WarehouseDto {

    private final UUID warehouseId;
    private final UUID hubId;
    private final String warehouseName;
    private final String address;
    private final String region;
    private final String contactPhone;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String updatedBy;

    public static WarehouseDto from(Warehouse warehouse) {
        return WarehouseDto.builder()
                .warehouseId(warehouse.getWarehouseId())
                .hubId(warehouse.getHubId())
                .warehouseName(warehouse.getWarehouseName())
                .address(warehouse.getAddress())
                .region(warehouse.getRegion())
                .contactPhone(warehouse.getContactPhone())
                .status(warehouse.getStatus().name())
                .createdAt(warehouse.getCreatedAt())
                .updatedAt(warehouse.getUpdatedAt())
                .updatedBy(warehouse.getUpdatedBy())
                .build();
    }
}
