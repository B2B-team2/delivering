package com.sparta.hubservice.warehouse.infrastructure.repository;

import com.sparta.hubservice.warehouse.domain.core.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseJpaRepository extends JpaRepository<Warehouse, UUID> {

    List<Warehouse> findAllByDeletedAtIsNull();

    Optional<Warehouse> findByWarehouseIdAndDeletedAtIsNull(UUID warehouseId);

    Optional<Warehouse> findByHubIdAndDeletedAtIsNull(UUID hubId);

    boolean existsByHubIdAndDeletedAtIsNull(UUID hubId);
}
