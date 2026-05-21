package com.sparta.companyservice.products.infrastructure.repository;

import com.sparta.companyservice.products.domain.core.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, UUID> {
    Optional<ProductOption> findByProductOptionIdAndDeletedAtIsNull(UUID productOptionId);
}
