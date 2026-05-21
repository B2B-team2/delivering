package com.sparta.hubservice.inventory.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.hubservice.inventory.application.dto.WarehouseInventoryDto;
import com.sparta.hubservice.inventory.application.service.InventoryService;
import com.sparta.hubservice.inventory.presentation.dto.WarehouseInventoryAdjustRequest;
import com.sparta.hubservice.inventory.presentation.dto.WarehouseInventoryCreateRequest;
import com.sparta.hubservice.inventory.presentation.dto.WarehouseInventoryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseInventoryResponse>> createInventory(
            @Valid @RequestBody WarehouseInventoryCreateRequest request) {
        WarehouseInventoryDto dto = inventoryService.createInventory(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(WarehouseInventoryResponse.from(dto)));
    }

    @GetMapping("/{inventory_id}")
    public ResponseEntity<ApiResponse<WarehouseInventoryResponse>> getInventory(
            @PathVariable UUID inventory_id) {
        WarehouseInventoryDto dto = inventoryService.getInventory(inventory_id);
        return ResponseEntity.ok(ApiResponse.success(WarehouseInventoryResponse.from(dto)));
    }

    @GetMapping("/warehouse/{warehouse_id}")
    public ResponseEntity<ApiResponse<List<WarehouseInventoryResponse>>> getInventoriesByWarehouse(
            @PathVariable UUID warehouse_id) {
        List<WarehouseInventoryResponse> responses = inventoryService.getInventoriesByWarehouse(warehouse_id).stream()
                .map(WarehouseInventoryResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PatchMapping("/{inventory_id}/adjust")
    public ResponseEntity<ApiResponse<WarehouseInventoryResponse>> adjustInventory(
            @PathVariable UUID inventory_id,
            @Valid @RequestBody WarehouseInventoryAdjustRequest request) {
        WarehouseInventoryDto dto = inventoryService.adjustInventory(inventory_id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(WarehouseInventoryResponse.from(dto)));
    }

    @DeleteMapping("/{inventory_id}")
    public ResponseEntity<ApiResponse<Void>> deleteInventory(
            @PathVariable UUID inventory_id,
            @RequestHeader(value = "X-User-Name", required = false, defaultValue = "system") String userName) {
        inventoryService.deleteInventory(inventory_id, userName);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
