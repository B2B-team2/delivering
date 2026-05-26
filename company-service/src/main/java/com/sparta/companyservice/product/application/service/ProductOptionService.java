package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductOptionCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductOptionDetailDto;
import com.sparta.companyservice.product.application.dto.ProductOptionDto;
import com.sparta.companyservice.product.application.dto.ProductOptionUpdateCommand;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.domain.repository.ProductOptionRepository;
import com.sparta.companyservice.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOptionService {

    private final ProductOptionRepository productOptionRepository;
    private final ProductRepository productRepository;

    public Map<UUID, ProductOptionDetailDto> getProductOptionDetails(List<UUID> productOptionIds) {
        List<ProductOption> options = productOptionRepository.findAllByIdsAndDeletedAtIsNull(productOptionIds);

        return options.stream()
                .collect(Collectors.toMap(
                        ProductOption::getProductOptionId,
                        ProductOptionDetailDto::from
                ));
    }

    @Transactional
    public ProductOptionDto createProductOption(ProductOptionCreateCommand command) {
        Product product = productRepository.findById(command.getProductId())
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_NOT_FOUND));

        ProductStatusEnum status = parseProductStatus(command.getStatus());

        ProductOption productOption = ProductOption.builder()
                .product(product)
                .optionsName(command.getOptionsName())
                .extraPrice(command.getExtraPrice())
                .status(status)
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

        ProductStatusEnum status = parseProductStatus(command.getStatus());

        productOption.update(
                command.getOptionsName(),
                command.getExtraPrice(),
                status,
                command.getDisplayOrder()
        );

        return ProductOptionDto.from(productOption);
    }

    @Transactional
    public ProductOptionDto deleteProductOption(UUID productOptionId, UUID deletedBy) {
        ProductOption productOption = productOptionRepository.findById(productOptionId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.PRODUCT_OPTION_NOT_FOUND));

        productOption.softDelete(deletedBy);

        return ProductOptionDto.from(productOption);
    }

    private ProductStatusEnum parseProductStatus(String status) {
        if (status == null) {
            return ProductStatusEnum.ON_SALE;
        }
        try {
            return ProductStatusEnum.valueOf(status);
        } catch (IllegalArgumentException e) {
            return ProductStatusEnum.ON_SALE;
        }
    }
}
