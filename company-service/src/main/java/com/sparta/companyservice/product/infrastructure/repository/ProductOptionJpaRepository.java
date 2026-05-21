package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, UUID> {
    Optional<ProductOption> findByProductOptionIdAndDeletedAtIsNull(UUID productOptionId);
}
