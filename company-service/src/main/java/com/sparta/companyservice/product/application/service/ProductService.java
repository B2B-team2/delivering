package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductDto;
import com.sparta.companyservice.product.application.dto.ProductStatusUpdateCommand;
import com.sparta.companyservice.product.application.dto.ProductUpdateCommand;
import com.sparta.companyservice.product.application.port.CompanyQueryPort;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.domain.repository.ProductCategoryRepository;
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
    private final ProductCategoryRepository categoryRepository;
    private final CompanyQueryPort companyQueryPort;

    @Transactional
    public ProductDto createProduct(ProductCreateCommand command) {
        validateCompanyAndCategory(command.getCompanyId(), command.getCategoryId());

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

        validateCompanyAndCategory(command.getCompanyId(), command.getCategoryId());

        ProductStatusEnum status = parseProductStatus(command.getStatus());

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
    public ProductDto deleteProduct(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_NOT_FOUND));

        product.softDelete("system");
        return ProductDto.from(product);
    }

    @Transactional
    public ProductDto updateProductStatus(UUID productId, ProductStatusUpdateCommand command) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_NOT_FOUND));

        ProductStatusEnum status = parseProductStatus(command.getStatus());

        product.updateStatus(status);

        return ProductDto.from(product);
    }

    private ProductStatusEnum parseProductStatus(String status) {
        try {
            return ProductStatusEnum.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CompanyErrorCode.INVALID_PRODUCT_STATUS);
        }
    }

    private void validateCompanyAndCategory(UUID companyId, UUID categoryId) {
        if (!companyQueryPort.existsCompanyById(companyId)) {
            throw new BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND);
        }
        if (categoryId != null && categoryRepository.findById(categoryId).isEmpty()) {
            throw new BusinessException(CompanyErrorCode.CATEGORY_NOT_FOUND);
        }
    }
}
