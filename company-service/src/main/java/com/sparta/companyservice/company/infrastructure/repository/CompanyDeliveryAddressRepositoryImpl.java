package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import com.sparta.companyservice.company.domain.repository.CompanyDeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CompanyDeliveryAddressRepositoryImpl implements CompanyDeliveryAddressRepository {

    private final CompanyDeliveryAddressJpaRepository deliveryAddressJpaRepository;

    @Override
    public CompanyDeliveryAddress save(CompanyDeliveryAddress address) {
        return deliveryAddressJpaRepository.save(address);
    }

    @Override
    public Optional<CompanyDeliveryAddress> findById(UUID addressId) {
        return deliveryAddressJpaRepository.findById(addressId);
    }

    @Override
    public void delete(CompanyDeliveryAddress address) {
        deliveryAddressJpaRepository.delete(address);
    }

    @Override
    public long count() {
        return deliveryAddressJpaRepository.count();
    }
}
