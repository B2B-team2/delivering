package com.sparta.companyservice.product.domain.repository;

import com.sparta.companyservice.product.domain.core.ProductOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductOptionRepository {
    ProductOption save(ProductOption productOption);
    Optional<ProductOption> findById(UUID productOptionId);
    Page<ProductOption> findAll(Pageable pageable);
    boolean existsById(UUID productOptionId);
    List<ProductOption> findAllByIdsAndDeletedAtIsNull(List<UUID> productOptionIds);
    void delete(ProductOption option);
    long count();
}
