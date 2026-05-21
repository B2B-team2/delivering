package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryAddressJpaRepository extends JpaRepository<DeliveryAddress, UUID> {
}
