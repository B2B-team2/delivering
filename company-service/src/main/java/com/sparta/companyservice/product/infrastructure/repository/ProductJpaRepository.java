package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
    Page<Product> findAllByDeletedAtIsNull(Pageable pageable);
    Optional<Product> findByProductIdAndDeletedAtIsNull(UUID productId);
}
