package com.sparta.companyservice.product.domain.repository;

import com.sparta.companyservice.product.domain.core.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID productId);
    Page<Product> findAll(Pageable pageable);
    long count();
}
