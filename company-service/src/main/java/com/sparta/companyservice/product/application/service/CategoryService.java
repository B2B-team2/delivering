package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.CategoryCreateCommand;
import com.sparta.companyservice.product.application.dto.CategoryDto;
import com.sparta.companyservice.product.application.dto.CategoryUpdateCommand;
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
public class CategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Transactional
    public CategoryDto createCategory(CategoryCreateCommand command) {
        if (categoryRepository.existsByName(command.getName())) {
            throw new BusinessException(CompanyErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        ProductCategory category = ProductCategory.builder()
                .name(command.getName())
                .depth(command.getDepth())
                .build();
        return CategoryDto.from(categoryRepository.save(category));
    }

    public Page<CategoryDto> getCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryDto::from);
    }

    public CategoryDto getCategory(UUID categoryId) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.CATEGORY_NOT_FOUND));
        return CategoryDto.from(category);
    }

    @Transactional
    public CategoryDto updateCategory(UUID categoryId, CategoryUpdateCommand command) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.CATEGORY_NOT_FOUND));
        
        category.update(command.getName(), command.getDepth());
        
        return CategoryDto.from(category);
    }

    @Transactional
    public CategoryDto deleteCategory(UUID categoryId, String deletedBy) {
        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(CompanyErrorCode.CATEGORY_NOT_FOUND));
        
        category.softDelete(deletedBy);
        
        return CategoryDto.from(category);
    }
}
