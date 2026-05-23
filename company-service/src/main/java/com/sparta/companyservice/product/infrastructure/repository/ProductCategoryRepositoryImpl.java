package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.ProductCategory;
import com.sparta.companyservice.product.domain.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductCategoryRepositoryImpl implements ProductCategoryRepository {

    private final ProductCategoryJpaRepository categoryJpaRepository;

    @Override
    public ProductCategory save(ProductCategory category) {
        return categoryJpaRepository.save(category);
    }

    @Override
    public Optional<ProductCategory> findById(UUID categoryId) {
        return categoryJpaRepository.findByCategoryIdAndDeletedAtIsNull(categoryId);
    }

    @Override
    public long count() {
        return categoryJpaRepository.count();
    }
}
