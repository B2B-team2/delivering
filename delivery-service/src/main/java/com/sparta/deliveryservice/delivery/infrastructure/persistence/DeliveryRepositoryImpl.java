package com.sparta.deliveryservice.delivery.infrastructure.persistence;

import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.domain.repository.DeliveryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class DeliveryRepositoryImpl implements DeliveryRepository {

    private final DeliveryJpaRepository deliveryJpaRepository;

    public DeliveryRepositoryImpl(DeliveryJpaRepository deliveryJpaRepository) {
        this.deliveryJpaRepository = deliveryJpaRepository;
    }

    @Override
    public Delivery save(Delivery delivery) {
        return deliveryJpaRepository.save(delivery);
    }

    @Override
    public Optional<Delivery> findById(UUID id) {
        return deliveryJpaRepository.findById(id);
    }

    @Override
    public Page<Delivery> findAll(Pageable pageable) { return deliveryJpaRepository.findAll(pageable); }

    @Override
    public Optional<Delivery> findByTrackingNumber(String trackingNumber) {return deliveryJpaRepository.findByTrackingNumber(trackingNumber);}
}