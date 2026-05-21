package com.sparta.companyservice.products.domain.repository;

import com.sparta.companyservice.products.domain.core.ProductCategory;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory category);
    Optional<ProductCategory> findById(UUID categoryId);
    void delete(ProductCategory category);
}
