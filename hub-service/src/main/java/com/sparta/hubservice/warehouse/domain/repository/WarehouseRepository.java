package com.sparta.hubservice.warehouse.domain.repository;

import com.sparta.hubservice.warehouse.domain.core.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository {

    Warehouse save(Warehouse warehouse);

    Optional<Warehouse> findById(UUID warehouseId);

    Optional<Warehouse> findByHubId(UUID hubId);

    List<Warehouse> findAll();

    Page<Warehouse> findAll(Pageable pageable);

    void delete(Warehouse warehouse, UUID deletedBy);
}
