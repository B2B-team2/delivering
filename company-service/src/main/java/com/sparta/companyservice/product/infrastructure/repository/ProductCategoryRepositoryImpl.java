package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.ProductCategory;
import com.sparta.companyservice.product.domain.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ProductCategory> findAll(Pageable pageable) {
        return categoryJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public long count() {
        return categoryJpaRepository.count();
    }

    @Override
    public boolean existsById(UUID categoryId) {
        return categoryJpaRepository.existsByCategoryIdAndDeletedAtIsNull(categoryId);
    }
}
