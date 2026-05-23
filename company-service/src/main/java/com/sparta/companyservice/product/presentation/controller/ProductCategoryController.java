package com.sparta.companyservice.product.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.companyservice.product.application.dto.ProductCategoryDto;
import com.sparta.companyservice.product.application.service.ProductCategoryService;
import com.sparta.companyservice.product.presentation.dto.ProductCategoryCreateRequest;
import com.sparta.companyservice.product.presentation.dto.ProductCategoryResponse;
import com.sparta.companyservice.product.presentation.dto.ProductCategoryUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
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
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> createCategory(@RequestBody @Valid ProductCategoryCreateRequest request) {
        ProductCategoryDto resultDto = categoryService.createCategory(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ProductCategoryResponse.from(resultDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductCategoryResponse>>> getCategories(
            @PageableDefault(size = 10)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "depth", direction = Sort.Direction.ASC),
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            }) Pageable pageable) {

        Pageable validatedPageable = PageableUtil.validatePageSize(pageable);

        PageResponse<ProductCategoryResponse> response = new PageResponse<>(
                categoryService.getCategories(validatedPageable).map(ProductCategoryResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> getCategory(@PathVariable UUID categoryId) {
        ProductCategoryDto resultDto = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success(ProductCategoryResponse.from(resultDto)));
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> updateCategory(
            @PathVariable UUID categoryId,
            @RequestBody @Valid ProductCategoryUpdateRequest request) {
        ProductCategoryDto resultDto = categoryService.updateCategory(categoryId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(ProductCategoryResponse.from(resultDto)));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<ProductCategoryResponse>> deleteCategory(@PathVariable UUID categoryId) {
        // TODO: 권한 로직 및 실제 사용자 정보 연동 시 수정 필요 ("system" 고정값 교체)
        ProductCategoryDto resultDto = categoryService.deleteCategory(categoryId, "system");
        return ResponseEntity.ok(ApiResponse.success(ProductCategoryResponse.from(resultDto)));
    }
}
