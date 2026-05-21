package com.sparta.companyservice.products.domain.repository;

import com.sparta.companyservice.products.domain.core.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(UUID productId);
    Page<Product> findAll(Pageable pageable);
    void delete(Product product);
}
