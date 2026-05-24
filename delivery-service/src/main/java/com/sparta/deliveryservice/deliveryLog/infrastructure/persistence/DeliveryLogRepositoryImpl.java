package com.sparta.deliveryservice.deliveryLog.infrastructure.persistence;

import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DeliveryLogRepositoryImpl implements DeliveryLogRepository {

    private final DeliveryLogJpaRepository deliveryLogJpaRepository;

    public DeliveryLogRepositoryImpl(DeliveryLogJpaRepository deliveryLogJpaRepository) {
        this.deliveryLogJpaRepository = deliveryLogJpaRepository;
    }

    @Override
    public DeliveryLog save(DeliveryLog deliveryLog) {
        return deliveryLogJpaRepository.save(deliveryLog);
    }

    @Override
    public Optional<DeliveryLog> findById(UUID id) {
        return deliveryLogJpaRepository.findById(id);
    }

    @Override
    public List<DeliveryLog> findAll() {
        return deliveryLogJpaRepository.findAll();
    }

    @Override
    public Page<DeliveryLog> findByDeliveryId(UUID deliveryId, Pageable pageable) {
        return deliveryLogJpaRepository.findByDeliveryId(deliveryId, pageable);
    }

}