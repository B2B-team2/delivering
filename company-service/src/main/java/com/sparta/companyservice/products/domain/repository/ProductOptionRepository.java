package com.sparta.companyservice.products.domain.repository;

import com.sparta.companyservice.products.domain.core.ProductOption;

import java.util.Optional;
import java.util.UUID;

public interface ProductOptionRepository {
    ProductOption save(ProductOption option);
    Optional<ProductOption> findById(UUID productOptionId);
    void delete(ProductOption option);
}
