package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.ProductOption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductOptionJpaRepository extends JpaRepository<ProductOption, UUID> {
    Optional<ProductOption> findByProductOptionIdAndDeletedAtIsNull(UUID productOptionId);
    Page<ProductOption> findAllByDeletedAtIsNull(Pageable pageable);
    boolean existsByProductOptionIdAndDeletedAtIsNull(UUID productOptionId);
    List<ProductOption> findAllByProductOptionIdInAndDeletedAtIsNull(List<UUID> productOptionIds);
}
