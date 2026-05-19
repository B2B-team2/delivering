package com.sparta.deliveryservice.delivery.infrastructure.persistence;

import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeliveryJpaRepository extends JpaRepository<Delivery, UUID> {
}