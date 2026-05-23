package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryJpaRepository extends JpaRepository<ProductCategory, UUID> {
    Optional<ProductCategory> findByCategoryIdAndDeletedAtIsNull(UUID categoryId);
    Page<ProductCategory> findAllByDeletedAtIsNull(Pageable pageable);
    boolean existsByCategoryIdAndDeletedAtIsNull(UUID categoryId);
    boolean existsByNameAndDeletedAtIsNull(String name);
}
