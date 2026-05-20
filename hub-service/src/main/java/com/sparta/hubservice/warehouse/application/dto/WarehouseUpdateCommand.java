package com.sparta.hubservice.warehouse.application.dto;

import com.sparta.hubservice.warehouse.domain.core.WarehouseStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseUpdateCommand {

    private final String warehouseName;
    private final String address;
    private final String region;
    private final String contactPhone;
    private final WarehouseStatus status;
}
