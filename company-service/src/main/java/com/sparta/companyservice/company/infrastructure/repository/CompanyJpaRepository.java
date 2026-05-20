package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data JPA Repository (Infrastructure 계층)
 */
public interface CompanyJpaRepository extends JpaRepository<Company, UUID> {
}
