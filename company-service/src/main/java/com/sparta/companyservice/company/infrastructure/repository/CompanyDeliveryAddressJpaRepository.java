package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.CompanyDeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyDeliveryAddressJpaRepository extends JpaRepository<CompanyDeliveryAddress, UUID> {
}
