package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.domain.repository.ProductOptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductOptionRepositoryImpl implements ProductOptionRepository {

    private final ProductOptionJpaRepository optionJpaRepository;

    @Override
    public ProductOption save(ProductOption option) {
        return optionJpaRepository.save(option);
    }

    @Override
    public Optional<ProductOption> findById(UUID productOptionId) {
        return optionJpaRepository.findByProductOptionIdAndDeletedAtIsNull(productOptionId);
    }

    @Override
    public void delete(ProductOption option) {
        option.softDelete(option.getCreatedBy()); // BaseEntity 삭제 로직
        optionJpaRepository.save(option);
    }
}
