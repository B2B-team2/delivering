package com.sparta.deliveryservice.deliveryRoute.infrastructure.repository;

import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRoute;
import com.sparta.deliveryservice.deliveryRoute.domain.repository.DeliveryRouteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DeliveryRouteRepositoryImpl implements DeliveryRouteRepository {

    private final DeliveryRouteJpaRepository deliveryRouteJpaRepository;

    public DeliveryRouteRepositoryImpl(DeliveryRouteJpaRepository deliveryRouteJpaRepository) {
        this.deliveryRouteJpaRepository = deliveryRouteJpaRepository;
    }

    @Override
    public DeliveryRoute save(DeliveryRoute deliveryRoute) {
        return deliveryRouteJpaRepository.save(deliveryRoute);
    }

    @Override
    public Optional<DeliveryRoute> findById(UUID id) {
        return deliveryRouteJpaRepository.findById(id);
    }

    @Override
    public List<DeliveryRoute> findAll() {
        return deliveryRouteJpaRepository.findAll();
    }

    @Override
    public List<DeliveryRoute> findByDeliveryId(UUID id) {
        return deliveryRouteJpaRepository.findByDeliveryId(id);
    }
}