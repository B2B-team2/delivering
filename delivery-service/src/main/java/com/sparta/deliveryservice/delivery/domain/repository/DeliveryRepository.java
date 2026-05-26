package com.sparta.deliveryservice.delivery.domain.repository;

import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(UUID id);
    Page<Delivery> findAll(Pageable pageable);
    Optional<Delivery> findByTrackingNumber(String trackingNumber);
    boolean existsByCompanyOrderId(UUID companyOrderId);
    List<Delivery> findByCompanyOrderId(UUID companyOrderId);
}
