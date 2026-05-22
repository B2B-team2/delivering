package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findByProductIdAndDeletedAtIsNull(productId);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return productJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

}
