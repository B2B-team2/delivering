package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductCategoryCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductCategoryDto;
import com.sparta.companyservice.product.application.dto.ProductCategoryUpdateCommand;
import com.sparta.companyservice.product.domain.core.ProductCategory;
import com.sparta.companyservice.product.domain.repository.ProductCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Transactional
    public ProductCategoryDto createCategory(ProductCategoryCreateCommand command) {
        if (categoryRepository.existsByName(command.getName())) {
            throw new BusinessException(CompanyErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        ProductCategory category = ProductCategory.builder()
                .name(command.getName())
                .depth(command.getDepth())
                .build();
        return ProductCategoryDto.from(categoryRepository.save(category));
    }

    public Page<ProductCategoryDto> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(ProductCategoryDto::from);
    }

    public ProductCategoryDto getCategory(UUID categoryId) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.CATEGORY_NOT_FOUND));
        return ProductCategoryDto.from(category);
    }

    @Transactional
    public ProductCategoryDto updateCategory(UUID categoryId, ProductCategoryUpdateCommand command) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getName().equals(command.getName()) && categoryRepository.existsByName(command.getName())) {
            throw new BusinessException(CompanyErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        category.update(command.getName(), command.getDepth());

        return ProductCategoryDto.from(category);
    }

    @Transactional
    public ProductCategoryDto deleteCategory(UUID categoryId, UUID deletedBy) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.CATEGORY_NOT_FOUND));

        category.softDelete(deletedBy);

        return ProductCategoryDto.from(category);
    }
}
