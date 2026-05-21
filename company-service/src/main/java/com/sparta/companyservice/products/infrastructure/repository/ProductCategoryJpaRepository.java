package com.sparta.companyservice.products.infrastructure.repository;

import com.sparta.companyservice.products.domain.core.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategory, UUID> {
    Optional<ProductCategory> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);
}
