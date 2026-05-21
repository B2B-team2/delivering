package com.sparta.hubservice.warehouse.presentation.dto;

import com.sparta.hubservice.warehouse.application.dto.WarehouseUpdateCommand;
import lombok.Getter;

@Getter
public class WarehouseUpdateRequest {

    private String warehouseName;
    private String address;
    private String region;
    private String contactPhone;
    private String status;

    public WarehouseUpdateCommand toCommand() {
        return WarehouseUpdateCommand.builder()
                .warehouseName(warehouseName)
                .address(address)
                .region(region)
                .contactPhone(contactPhone)
                .status(status)
                .build();
    }
}
