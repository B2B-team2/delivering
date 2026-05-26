package com.sparta.companyservice.company.domain.repository;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;

import java.util.Optional;
import java.util.UUID;

public interface CompanyDeliveryAddressRepository {
    CompanyDeliveryAddress save(CompanyDeliveryAddress address);
    Optional<CompanyDeliveryAddress> findById(UUID addressId);
    void delete(CompanyDeliveryAddress address);
    long count();
    void updateAllIsDefaultFalseByCompanyId(UUID companyId);
}
