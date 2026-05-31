package com.sparta.companyservice.company.domain.repository;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CompanyDeliveryAddressRepository {
    CompanyDeliveryAddress save(CompanyDeliveryAddress address);
    Optional<CompanyDeliveryAddress> findById(UUID addressId);
    Optional<CompanyDeliveryAddress> findDefaultAddressByCompanyId(UUID companyId);
    void delete(CompanyDeliveryAddress address);
    long count();
    void updateAllIsDefaultFalseByCompanyId(UUID companyId);
    Page<CompanyDeliveryAddress> findAllByCompanyIdAndDeletedAtIsNull(UUID companyId, Pageable pageable);
}
