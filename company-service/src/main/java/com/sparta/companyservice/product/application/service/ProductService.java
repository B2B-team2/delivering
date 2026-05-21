package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductDto;
import com.sparta.companyservice.product.application.dto.ProductUpdateCommand;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.domain.repository.ProductRepository;
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
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_NOT_FOUND));
        return ProductDto.from(product);
    }

    public Page<ProductDto> getProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(ProductDto::from);
    }

    @Transactional
    public ProductDto updateProduct(UUID productId, ProductUpdateCommand command) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_NOT_FOUND));

        ProductStatusEnum status;
        try {
            status = ProductStatusEnum.valueOf(command.getStatus());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CompanyErrorCode.INVALID_PRODUCT_STATUS);
        }

        product.update(
                command.getCompanyId(),
                command.getCategoryId(),
                command.getName(),
                command.getPrice(),
                command.getDescription(),
                command.getThumbnailUrl(),
                status
        );

        return ProductDto.from(product);
    }

    @Transactional
    public void deleteProduct(UUID productId) {
        // TODO: 구현 예정
    }
}
