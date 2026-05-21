package com.sparta.deliveryservice.deliveryRoute.infrastructure.repository;

import com.sparta.deliveryservice.deliveryRoute.domin.core.DeliveryRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryRouteJpaRepository extends JpaRepository<DeliveryRoute, UUID> {
    List<DeliveryRoute> findByDeliveryId(UUID routeId);
}
