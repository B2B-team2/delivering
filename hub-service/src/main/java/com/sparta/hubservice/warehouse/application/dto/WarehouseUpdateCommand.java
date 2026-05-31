package com.sparta.hubservice.warehouse.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WarehouseUpdateCommand {

    private final String warehouseName;
    private final String address;
    private final String region;
    private final String contactPhone;
    private final String status;
}
