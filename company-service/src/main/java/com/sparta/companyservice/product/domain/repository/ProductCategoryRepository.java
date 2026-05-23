package com.sparta.companyservice.product.domain.repository;

import com.sparta.companyservice.product.domain.core.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory category);
    Optional<ProductCategory> findById(UUID categoryId);
    Page<ProductCategory> findAll(Pageable pageable);
    long count();
    boolean existsById(UUID categoryId);
    boolean existsByName(String name);
}
