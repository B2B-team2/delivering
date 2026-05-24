package com.sparta.deliveryservice.deliveryLog.infrastructure.persistence;

import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryLogJpaRepository extends JpaRepository<DeliveryLog, UUID> {
    Page<DeliveryLog> findByDeliveryId(UUID deliveryId, Pageable pageable);
}