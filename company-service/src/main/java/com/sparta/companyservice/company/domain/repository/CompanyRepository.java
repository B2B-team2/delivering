package com.sparta.companyservice.company.domain.repository;

import com.sparta.companyservice.company.domain.core.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Domain Repository Interface (순수 Java 인터페이스)
 * 기술 세부사항(Spring Data JPA 등)에 의존하지 않음
 */
public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findById(UUID id);
    Page<Company> findAll(Pageable pageable);
    long count();
}
