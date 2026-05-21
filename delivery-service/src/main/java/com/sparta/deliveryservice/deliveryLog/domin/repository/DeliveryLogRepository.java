package com.sparta.deliveryservice.deliveryLog.domin.repository;

import com.sparta.deliveryservice.deliveryLog.domin.core.DeliveryLog;
import com.sparta.deliveryservice.deliveryRoute.domin.core.DeliveryRoute;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryLogRepository {
    DeliveryLog save(DeliveryLog deliveryLog);
    Optional<DeliveryLog> findById(UUID id);
    List<DeliveryLog> findAll();
}
