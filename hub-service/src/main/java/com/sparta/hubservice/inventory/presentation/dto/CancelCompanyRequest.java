package com.sparta.hubservice.inventory.presentation.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CancelCompanyRequest {

    @NotNull
    private UUID companyOrderId;
}
