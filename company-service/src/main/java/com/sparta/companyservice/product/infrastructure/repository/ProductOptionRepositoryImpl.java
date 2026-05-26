package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.domain.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductOptionRepositoryImpl implements ProductOptionRepository {

    private final ProductOptionJpaRepository productOptionJpaRepository;

    @Override
    public ProductOption save(ProductOption productOption) {
        return productOptionJpaRepository.save(productOption);
    }

    @Override
    public Optional<ProductOption> findById(UUID productOptionId) {
        return productOptionJpaRepository.findByProductOptionIdAndDeletedAtIsNull(productOptionId);
    }

    @Override
    public Page<ProductOption> findAll(Pageable pageable) {
        return productOptionJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public boolean existsById(UUID productOptionId) {
        return productOptionJpaRepository.existsByProductOptionIdAndDeletedAtIsNull(productOptionId);
    }

    @Override
    public List<ProductOption> findAllByIdsAndDeletedAtIsNull(List<UUID> productOptionIds) {
        return productOptionJpaRepository.findAllByProductOptionIdInAndDeletedAtIsNull(productOptionIds);
    }

    @Override
    public void delete(ProductOption option) {
        option.softDelete(option.getCreatedBy());
        productOptionJpaRepository.save(option);
    }

    @Override
    public long count() {
        return productOptionJpaRepository.count();
    }
}
