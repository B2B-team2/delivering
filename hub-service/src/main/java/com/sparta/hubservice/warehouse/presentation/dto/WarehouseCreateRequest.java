package com.sparta.hubservice.warehouse.presentation.dto;

import com.sparta.hubservice.warehouse.application.dto.WarehouseCreateCommand;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class WarehouseCreateRequest {

    @NotNull
    private UUID hubId;

    private String warehouseName;
    private String address;
    private String region;
    private String contactPhone;
    private String status;

    public WarehouseCreateCommand toCommand() {
        return WarehouseCreateCommand.builder()
                .hubId(hubId)
                .warehouseName(warehouseName)
                .address(address)
                .region(region)
                .contactPhone(contactPhone)
                .status(status)
                .build();
    }
}
