package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.DeliveryAddress;
import com.sparta.companyservice.company.domain.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DeliveryAddressRepositoryImpl implements DeliveryAddressRepository {

    private final DeliveryAddressJpaRepository deliveryAddressJpaRepository;

    @Override
    public DeliveryAddress save(DeliveryAddress address) {
        return deliveryAddressJpaRepository.save(address);
    }

    @Override
    public Optional<DeliveryAddress> findById(UUID addressId) {
        return deliveryAddressJpaRepository.findById(addressId);
    }

    @Override
    public void delete(DeliveryAddress address) {
        deliveryAddressJpaRepository.delete(address);
    }

    @Override
    public long count() {
        return deliveryAddressJpaRepository.count();
    }
}
