package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CompanyDeliveryAddressJpaRepository extends JpaRepository<CompanyDeliveryAddress, UUID> {
    @Modifying
    @Query("UPDATE CompanyDeliveryAddress c SET c.isDefault = false WHERE c.companyId = :companyId AND c.isDefault = true")
    void updateAllIsDefaultFalseByCompanyId(@Param("companyId") UUID companyId);

    Page<CompanyDeliveryAddress> findAllByCompanyIdAndDeletedAtIsNull(UUID companyId, Pageable pageable);

    Optional<CompanyDeliveryAddress> findByAddressIdAndDeletedAtIsNull(UUID addressId);

    Optional<CompanyDeliveryAddress> findByCompanyIdAndIsDefaultTrueAndDeletedAtIsNull(UUID companyId);
}
