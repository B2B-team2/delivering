package com.sparta.hubservice.warehouse.infrastructure.repository;

import com.sparta.hubservice.warehouse.domain.core.Warehouse;
import com.sparta.hubservice.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class WarehouseRepositoryImpl implements WarehouseRepository {

    private final WarehouseJpaRepository warehouseJpaRepository;

    @Override
    public Warehouse save(Warehouse warehouse) {
        return warehouseJpaRepository.save(warehouse);
    }

    @Override
    public Optional<Warehouse> findById(UUID warehouseId) {
        return warehouseJpaRepository.findByWarehouseIdAndDeletedAtIsNull(warehouseId);
    }

    @Override
    public Optional<Warehouse> findByHubId(UUID hubId) {
        return warehouseJpaRepository.findByHubIdAndDeletedAtIsNull(hubId);
    }

    @Override
    public List<Warehouse> findAll() {
        return warehouseJpaRepository.findAllByDeletedAtIsNull();
    }

    @Override
    public Page<Warehouse> findAll(Pageable pageable) {
        return warehouseJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public void delete(Warehouse warehouse, String deletedBy) {
        warehouse.softDelete(deletedBy);
        warehouseJpaRepository.save(warehouse);
    }

    public boolean existsByHubId(UUID hubId) {
        return warehouseJpaRepository.existsByHubIdAndDeletedAtIsNull(hubId);
    }
}
