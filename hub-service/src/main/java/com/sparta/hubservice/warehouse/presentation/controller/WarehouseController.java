package com.sparta.hubservice.warehouse.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.PageResponse;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.warehouse.application.dto.WarehouseDto;
import com.sparta.hubservice.warehouse.application.service.WarehouseService;
import com.sparta.hubservice.warehouse.presentation.dto.WarehouseCreateRequest;
import com.sparta.hubservice.warehouse.presentation.dto.WarehouseResponse;
import com.sparta.hubservice.warehouse.presentation.dto.WarehouseUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private static final Set<Integer> ALLOWED_SIZES = Set.of(10, 30, 50);

    private final WarehouseService warehouseService;

    @PostMapping
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hub-Id", required = false) UUID hubId,
            @Valid @RequestBody WarehouseCreateRequest request) {
        requireMasterOrHubManager(role);
        if ("HUB_MANAGER".equals(role)) {
            if (hubId == null || !hubId.equals(request.getHubId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }
        WarehouseDto dto = warehouseService.createWarehouse(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(WarehouseResponse.from(dto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<WarehouseResponse>>> getAllWarehouses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,DESC") String sort) {
        int validSize = ALLOWED_SIZES.contains(size) ? size : 10;
        String[] sortParts = sort.split(",");
        Sort sortObj = sortParts.length > 1
                ? Sort.by(Sort.Direction.fromString(sortParts[1].trim()), sortParts[0].trim())
                : Sort.by(sortParts[0].trim());
        PageResponse<WarehouseResponse> result = new PageResponse<>(
                warehouseService.getAllWarehouses(PageRequest.of(page, validSize, sortObj))
                        .map(WarehouseResponse::from));
        return ResponseEntity.ok(ApiResponse.success(result));
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
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hub-Id", required = false) UUID hubId,
            @PathVariable UUID warehouse_id,
            @RequestBody WarehouseUpdateRequest request) {
        requireMasterOrHubManager(role);
        if ("HUB_MANAGER".equals(role) && hubId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        UUID requesterHubId = "HUB_MANAGER".equals(role) ? hubId : null;
        WarehouseDto dto = warehouseService.updateWarehouse(warehouse_id, request.toCommand(), requesterHubId);
        return ResponseEntity.ok(ApiResponse.success(WarehouseResponse.from(dto)));
    }

    @DeleteMapping("/{warehouse_id}")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(
            @RequestHeader("X-User-Role") String role,
            @RequestHeader(value = "X-Hub-Id", required = false) UUID hubId,
            @PathVariable UUID warehouse_id,
            @RequestHeader("X-User-Id") UUID userId) {
        requireMasterOrHubManager(role);
        if ("HUB_MANAGER".equals(role) && hubId == null) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        UUID requesterHubId = "HUB_MANAGER".equals(role) ? hubId : null;
        warehouseService.deleteWarehouse(warehouse_id, userId, requesterHubId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    private void requireMasterOrHubManager(String role) {
        if (!"MASTER".equals(role) && !"HUB_MANAGER".equals(role)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
