package com.sparta.hubservice.inventory.presentation.controller;

import com.sparta.hubservice.inventory.application.dto.InventoryItemCommand;
import com.sparta.hubservice.inventory.application.service.InventoryService;
import com.sparta.hubservice.inventory.presentation.dto.CancelCompanyRequest;
import com.sparta.hubservice.inventory.presentation.dto.CancelReservationRequest;
import com.sparta.hubservice.inventory.presentation.dto.InventoryBulkRequest;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> reserve(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.reserveStock(request.getOrderId(), request.getCompanyOrderId(), toCommands(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancel(@Valid @RequestBody CancelReservationRequest request) {
        inventoryService.cancelReservation(request.getOrderId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cancel/company")
    public ResponseEntity<Void> cancelCompany(@Valid @RequestBody CancelCompanyRequest request) {
        inventoryService.cancelCompanyReservation(request.getCompanyOrderId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deduct")
    public ResponseEntity<Void> deduct(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.deductStock(request.getOrderId(), toCommands(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/return")
    public ResponseEntity<Void> returnStock(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.returnStock(request.getOrderId(), toCommands(request));
        return ResponseEntity.noContent().build();
    }

    private List<InventoryItemCommand> toCommands(InventoryBulkRequest request) {
        return request.getItems().stream()
                .map(i -> new InventoryItemCommand(i.getProductOptionId(), i.getQuantity()))
                .toList();
    }
}
