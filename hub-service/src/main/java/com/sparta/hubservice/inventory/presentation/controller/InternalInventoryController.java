package com.sparta.hubservice.inventory.presentation.controller;

import com.sparta.common.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<Void>> reserve(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.reserveStock(request.getOrderId(), request.getCompanyOrderId(), toCommands(request));
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancel(@Valid @RequestBody CancelReservationRequest request) {
        inventoryService.cancelReservation(request.getOrderId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/cancel/company")
    public ResponseEntity<ApiResponse<Void>> cancelCompany(@Valid @RequestBody CancelCompanyRequest request) {
        inventoryService.cancelCompanyReservation(request.getCompanyOrderId());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/deduct")
    public ResponseEntity<ApiResponse<Void>> deduct(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.deductStock(request.getOrderId(), toCommands(request));
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<Void>> returnStock(@Valid @RequestBody InventoryBulkRequest request) {
        inventoryService.returnStock(request.getOrderId(), toCommands(request));
        return ResponseEntity.ok(ApiResponse.success());
    }

    private List<InventoryItemCommand> toCommands(InventoryBulkRequest request) {
        return request.getItems().stream()
                .map(i -> new InventoryItemCommand(i.getProductOptionId(), i.getQuantity()))
                .toList();
    }
}
