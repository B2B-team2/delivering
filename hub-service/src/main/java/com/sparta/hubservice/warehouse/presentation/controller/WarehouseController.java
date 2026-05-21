package com.sparta.hubservice.warehouse.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.hubservice.warehouse.application.dto.WarehouseDto;
import com.sparta.hubservice.warehouse.application.service.WarehouseService;
import com.sparta.hubservice.warehouse.presentation.dto.WarehouseCreateRequest;
import com.sparta.hubservice.warehouse.presentation.dto.WarehouseResponse;
import com.sparta.hubservice.warehouse.presentation.dto.WarehouseUpdateRequest;
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
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @Valid @RequestBody WarehouseCreateRequest request) {
        WarehouseDto dto = warehouseService.createWarehouse(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(WarehouseResponse.from(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses() {
        List<WarehouseResponse> responses = warehouseService.getAllWarehouses().stream()
                .map(WarehouseResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{warehouse_id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouse(
            @PathVariable UUID warehouse_id) {
        WarehouseDto dto = warehouseService.getWarehouse(warehouse_id);
        return ResponseEntity.ok(ApiResponse.success(WarehouseResponse.from(dto)));
    }

    @GetMapping("/hub/{hub_id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseByHubId(
            @PathVariable UUID hub_id) {
        WarehouseDto dto = warehouseService.getWarehouseByHubId(hub_id);
        return ResponseEntity.ok(ApiResponse.success(WarehouseResponse.from(dto)));
    }

    @PatchMapping("/{warehouse_id}")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(
            @PathVariable UUID warehouse_id,
            @RequestBody WarehouseUpdateRequest request) {
        WarehouseDto dto = warehouseService.updateWarehouse(warehouse_id, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(WarehouseResponse.from(dto)));
    }

    @DeleteMapping("/{warehouse_id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(
            @PathVariable UUID warehouse_id,
            @RequestHeader(value = "X-User-Name", required = false, defaultValue = "system") String userName) {
        warehouseService.deleteWarehouse(warehouse_id, userName);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
