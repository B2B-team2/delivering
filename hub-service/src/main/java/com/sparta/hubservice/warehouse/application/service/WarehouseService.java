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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
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

    public Page<WarehouseDto> getAllWarehouses(Pageable pageable) {
        return warehouseRepository.findAll(pageable).map(WarehouseDto::from);
    }

    public WarehouseDto getWarehouse(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return WarehouseDto.from(warehouse);
    }

    public WarehouseDto getWarehouseByHubId(UUID hubId) {
        Warehouse warehouse = warehouseRepository.findByHubId(hubId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        return WarehouseDto.from(warehouse);
    }

    @Transactional
    public WarehouseDto updateWarehouse(UUID warehouseId, WarehouseUpdateCommand command, UUID requesterHubId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (requesterHubId != null && !requesterHubId.equals(warehouse.getHubId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        WarehouseStatus status = command.getStatus() != null ? WarehouseStatus.valueOf(command.getStatus()) : null;
        warehouse.update(command.getWarehouseName(), command.getAddress(), command.getRegion(),
                command.getContactPhone(), status);
        return WarehouseDto.from(warehouse);
    }

    @Transactional
    public void deleteWarehouse(UUID warehouseId, UUID deletedBy, UUID requesterHubId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WAREHOUSE_NOT_FOUND));
        if (requesterHubId != null && !requesterHubId.equals(warehouse.getHubId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        warehouse.softDelete(deletedBy);
        warehouseRepository.save(warehouse);
    }
}
