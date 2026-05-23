package com.sparta.companyservice.product.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.companyservice.product.application.dto.CategoryDto;
import com.sparta.companyservice.product.application.service.CategoryService;
import com.sparta.companyservice.product.presentation.dto.CategoryCreateRequest;
import com.sparta.companyservice.product.presentation.dto.CategoryResponse;
import com.sparta.companyservice.product.presentation.dto.CategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@RequestBody @Valid CategoryCreateRequest request) {
        CategoryDto resultDto = categoryService.createCategory(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(CategoryResponse.from(resultDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CategoryResponse>>> getCategories(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Pageable validatedPageable = PageableUtil.validatePageSize(pageable);

        PageResponse<CategoryResponse> response = new PageResponse<>(
                categoryService.getCategories(validatedPageable).map(CategoryResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable UUID categoryId) {
        CategoryDto resultDto = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(resultDto)));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @RequestBody @Valid CategoryUpdateRequest request) {
        CategoryDto resultDto = categoryService.updateCategory(categoryId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(resultDto)));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryResponse>> deleteCategory(@PathVariable UUID categoryId) {
        // TODO: 권한 로직 및 실제 사용자 정보 연동 시 수정 필요 ("system" 고정값 교체)
        CategoryDto resultDto = categoryService.deleteCategory(categoryId, "system");
        return ResponseEntity.ok(ApiResponse.success(CategoryResponse.from(resultDto)));
    }
}
