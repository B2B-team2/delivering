package com.sparta.hubservice.warehouse.presentation.dto;

import com.sparta.hubservice.warehouse.application.dto.WarehouseDto;
import com.sparta.hubservice.warehouse.domain.core.WarehouseStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class WarehouseResponse {

    private final UUID warehouseId;
    private final UUID hubId;
    private final String warehouseName;
    private final String address;
    private final String region;
    private final String contactPhone;
    private final WarehouseStatus status;

    public static WarehouseResponse from(WarehouseDto dto) {
        return WarehouseResponse.builder()
                .warehouseId(dto.getWarehouseId())
                .hubId(dto.getHubId())
                .warehouseName(dto.getWarehouseName())
                .address(dto.getAddress())
                .region(dto.getRegion())
                .contactPhone(dto.getContactPhone())
                .status(dto.getStatus())
                .build();
    }
}
