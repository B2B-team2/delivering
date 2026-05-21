package com.sparta.hubservice.warehouse.domain.repository;

import com.sparta.hubservice.warehouse.domain.core.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(UUID warehouseId);

    Optional<Warehouse> findByHubId(UUID hubId);

    List<Warehouse> findAll();

    void delete(Warehouse warehouse);
}
