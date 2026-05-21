package com.sparta.companyservice.products.application.service;

import com.sparta.companyservice.products.application.dto.ProductCreateCommand;
import com.sparta.companyservice.products.application.dto.ProductDto;
import com.sparta.companyservice.products.domain.core.Product;
import com.sparta.companyservice.products.domain.core.ProductStatusEnum;
import com.sparta.companyservice.products.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductDto createProduct(ProductCreateCommand command) {
        Product product = Product.builder()
                .companyId(command.getCompanyId())
                .categoryId(command.getCategoryId())
                .name(command.getName())
                .price(command.getPrice())
                .description(command.getDescription())
                .thumbnailUrl(command.getThumbnailUrl())
                .status(ProductStatusEnum.ON_SALE)
                .build();

        Product savedProduct = productRepository.save(product);
        return ProductDto.from(savedProduct);
    }

    public ProductDto getProduct(UUID productId) {
        // TODO: 구현 예정
        return null;
    }

    public Page<ProductDto> getProducts(Pageable pageable) {
        // TODO: 구현 예정
        return null;
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        // TODO: 구현 예정
    }
}
