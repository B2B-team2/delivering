package com.sparta.companyservice.company.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain Repository 구현체 (Infrastructure 계층)
 * Domain의 포트(인터페이스)를 구현하여 DIP 적용
 */
@Repository
@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepository {

    private final CompanyJpaRepository jpaRepository;

    @Override
    public Company save(Company company) {
        return jpaRepository.save(company);
    }

    @Override
    public Optional<Company> findById(UUID id) {
        return jpaRepository.findByCompanyIdAndDeletedAtIsNull(id);
    }

    @Override
    public Page<Company> findAll(Pageable pageable) {
        return jpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public Optional<Company> findByBusinessNumberAnyStatus(String businessNumber) {
        return jpaRepository.findByBusinessNumber(businessNumber);
    }

    @Override
    public List<Company> findAllByCompanyIdIn(List<UUID> companyIds) {
        return jpaRepository.findAllByCompanyIdInAndDeletedAtIsNull(companyIds);
    }

    @Override
    public boolean existsByHubId(UUID hubId) {
        return jpaRepository.existsByHubIdAndDeletedAtIsNull(hubId);
    }
}
