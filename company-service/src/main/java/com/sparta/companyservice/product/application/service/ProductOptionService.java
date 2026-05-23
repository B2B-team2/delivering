package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductOptionCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductOptionDto;
import com.sparta.companyservice.product.application.dto.ProductOptionUpdateCommand;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.domain.repository.ProductOptionRepository;
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
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductOptionDto createProductOption(ProductOptionCreateCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_NOT_FOUND));

        ProductOption productOption = ProductOption.builder()
                .product(product)
                .optionsName(command.getOptionsName())
                .extraPrice(command.getExtraPrice())
                .status(command.getStatus())
                .displayOrder(command.getDisplayOrder())
                .build();

        return ProductOptionDto.from(productOptionRepository.save(productOption));
    }

    public Page<ProductOptionDto> getProductOptions(Pageable pageable) {
        return productOptionRepository.findAll(pageable)
                .map(ProductOptionDto::from);
    }

    public ProductOptionDto getProductOption(UUID productOptionId) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_OPTION_NOT_FOUND));
        return ProductOptionDto.from(productOption);
    }

    @Transactional
    public ProductOptionDto updateProductOption(UUID productOptionId, ProductOptionUpdateCommand command) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_OPTION_NOT_FOUND));

        productOption.update(
                command.getOptionsName(),
                command.getExtraPrice(),
                command.getStatus(),
                command.getDisplayOrder()
        );

        return ProductOptionDto.from(productOption);
    }

    @Transactional
    public ProductOptionDto deleteProductOption(UUID productOptionId, String deletedBy) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_OPTION_NOT_FOUND));

        productOption.softDelete(deletedBy);

        return ProductOptionDto.from(productOption);
    }
}
