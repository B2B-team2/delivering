package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository (Infrastructure 계층)
 */
public interface CompanyJpaRepository extends JpaRepository<Company, UUID> {
    Page<Company> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Company> findByCompanyIdAndDeletedAtIsNull(UUID companyId);
    Optional<Company> findByBusinessNumber(String businessNumber);
    List<Company> findAllByCompanyIdInAndDeletedAtIsNull(List<UUID> companyIds);
}
