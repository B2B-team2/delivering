package com.sparta.companyservice.products.infrastructure.repository;

import com.sparta.companyservice.products.domain.core.ProductCategory;
import com.sparta.companyservice.products.domain.repository.ProductCategoryRepository;
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
    public void delete(ProductCategory category) {
        category.softDelete(category.getCreatedBy()); // BaseEntity 삭제 로직
        categoryJpaRepository.save(category);
    }
}
