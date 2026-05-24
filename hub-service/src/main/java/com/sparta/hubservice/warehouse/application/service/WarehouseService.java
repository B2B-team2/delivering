package com.sparta.hubservice.warehouse.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.warehouse.application.dto.WarehouseCreateCommand;
import com.sparta.hubservice.warehouse.application.dto.WarehouseDto;
import com.sparta.hubservice.warehouse.application.dto.WarehouseUpdateCommand;
import com.sparta.hubservice.warehouse.domain.core.Warehouse;
import com.sparta.hubservice.warehouse.domain.core.WarehouseStatus;
import com.sparta.hubservice.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseDto createWarehouse(WarehouseCreateCommand command) {
        if (warehouseRepository.findByHubId(command.getHubId()).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATE_WAREHOUSE);
        }

        Warehouse warehouse = Warehouse.builder()
                .hubId(command.getHubId())
                .warehouseName(command.getWarehouseName())
                .address(command.getAddress())
                .region(command.getRegion())
                .contactPhone(command.getContactPhone())
                .status(command.getStatus() != null ? WarehouseStatus.valueOf(command.getStatus()) : WarehouseStatus.ACTIVE)
                .build();

        return WarehouseDto.from(warehouseRepository.save(warehouse));
    }

    @Cacheable(value = "warehouses", key = "'all'")
    public List<WarehouseDto> getAllWarehouses() {
        return warehouseRepository.findAll().stream()
                .map(WarehouseDto::from)
                .toList();
    }

    @Cacheable(value = "warehouses", key = "#warehouseId")
    public WarehouseDto getWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return WarehouseDto.from(warehouse);
    }

    @Cacheable(value = "warehouses", key = "'hub_' + #hubId")
    public WarehouseDto getWarehouseByHubId(UUID hubId) {
        Warehouse warehouse = warehouseRepository.findByHubId(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return WarehouseDto.from(warehouse);
    }

    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public WarehouseDto updateWarehouse(UUID warehouseId, WarehouseUpdateCommand command) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        WarehouseStatus status = command.getStatus() != null ? WarehouseStatus.valueOf(command.getStatus()) : null;
        warehouse.update(command.getWarehouseName(), command.getAddress(), command.getRegion(),
                command.getContactPhone(), status);
        return WarehouseDto.from(warehouse);
    }

    @Transactional
    @CacheEvict(value = "warehouses", allEntries = true)
    public void deleteWarehouse(UUID warehouseId, UUID deletedBy) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        warehouse.softDelete(deletedBy);
        warehouseRepository.save(warehouse);
    }
}
