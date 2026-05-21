package com.sparta.companyservice.products.infrastructure.repository;

import com.sparta.companyservice.products.domain.core.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {
}
