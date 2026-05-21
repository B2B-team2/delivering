package com.sparta.companyservice.products.infrastructure.repository;

import com.sparta.companyservice.products.domain.core.ProductOption;
import com.sparta.companyservice.products.domain.repository.ProductOptionRepository;
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
