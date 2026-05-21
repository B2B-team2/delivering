package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
}
