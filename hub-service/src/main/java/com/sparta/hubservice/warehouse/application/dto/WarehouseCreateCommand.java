package com.sparta.hubservice.warehouse.application.dto;

import com.sparta.hubservice.warehouse.domain.core.WarehouseStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class WarehouseCreateCommand {

    private final UUID hubId;
    private final String warehouseName;
    private final String address;
    private final String region;
    private final String contactPhone;
    private final WarehouseStatus status;
}
