package com.sparta.deliveryservice.deliveryRoute.domin.repository;

import com.sparta.deliveryservice.deliveryRoute.domin.core.DeliveryRoute;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRouteRepository {
    DeliveryRoute save(DeliveryRoute deliveryRoute);
    Optional<DeliveryRoute> findById(UUID id);
    List<DeliveryRoute> findAll();
    List<DeliveryRoute> findByDeliveryId(UUID id);
}
