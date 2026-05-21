package com.sparta.companyservice.product.domain.repository;

import com.sparta.companyservice.product.domain.core.ProductCategory;

import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory category);
    Optional<ProductCategory> findById(UUID categoryId);
    void delete(ProductCategory category);
}
