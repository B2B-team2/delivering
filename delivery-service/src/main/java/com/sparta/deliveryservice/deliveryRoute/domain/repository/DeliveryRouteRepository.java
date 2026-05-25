package com.sparta.deliveryservice.deliveryRoute.domain.repository;

import com.sparta.deliveryservice.deliveryRoute.domain.core.DeliveryRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRepository {
    DeliveryRoute save(DeliveryRoute deliveryRoute);
    Optional<DeliveryRoute> findById(UUID id);
    List<DeliveryRoute> findAll();
    List<DeliveryRoute> findByDeliveryId(UUID id);
}
