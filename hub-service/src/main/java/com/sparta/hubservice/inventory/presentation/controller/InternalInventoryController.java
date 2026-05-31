package com.sparta.hubservice.inventory.presentation.controller;

import com.sparta.hubservice.inventory.application.dto.InventoryItemCommand;
import com.sparta.hubservice.inventory.application.service.InventoryService;
import com.sparta.hubservice.inventory.presentation.dto.CancelCompanyRequest;
import com.sparta.hubservice.inventory.presentation.dto.CancelReservationRequest;
import com.sparta.hubservice.inventory.presentation.dto.InventoryBulkRequest;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/internal/inventory")
@RequiredArgsConstructor
public class InternalInventoryController {

    private final InventoryService inventoryService;

    @PostMapping("/reserve")
    public void reserve(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.reserveStock(request.getOrderId(), request.getCompanyOrderId(), toCommands(request));
    }

    @PostMapping("/cancel")
    public void cancel(@Valid @RequestBody CancelReservationRequest request) {
        inventoryService.cancelReservation(request.getOrderId());
    }

    @PostMapping("/cancel/company")
    public void cancelCompany(@Valid @RequestBody CancelCompanyRequest request) {
        inventoryService.cancelCompanyReservation(request.getCompanyOrderId());
    }

    @PostMapping("/deduct")
    public void deduct(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.deductStock(request.getOrderId(), toCommands(request));
    }

    @PostMapping("/return")
    public void returnStock(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.returnStock(request.getOrderId(), toCommands(request));
    }

    private List<InventoryItemCommand> toCommands(InventoryBulkRequest request) {
        return request.getItems().stream()
                .map(i -> new InventoryItemCommand(i.getProductOptionId(), i.getQuantity()))
                .toList();
    }
}
