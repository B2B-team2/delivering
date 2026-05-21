package com.sparta.companyservice.company.domain.repository;

import com.sparta.companyservice.company.domain.core.DeliveryAddress;

import java.util.Optional;
import java.util.UUID;

public interface DeliveryAddressRepository {
    DeliveryAddress save(DeliveryAddress address);
    Optional<DeliveryAddress> findById(UUID addressId);
    void delete(DeliveryAddress address);
    long count();
}
