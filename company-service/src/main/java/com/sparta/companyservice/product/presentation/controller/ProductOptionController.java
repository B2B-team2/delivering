package com.sparta.companyservice.product.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.companyservice.product.application.dto.ProductOptionDto;
import com.sparta.companyservice.product.application.service.ProductOptionService;
import com.sparta.companyservice.product.presentation.dto.ProductOptionCreateRequest;
import com.sparta.companyservice.product.presentation.dto.ProductOptionDeleteResponse;
import com.sparta.companyservice.product.presentation.dto.ProductOptionResponse;
import com.sparta.companyservice.product.presentation.dto.ProductOptionUpdateRequest;
import com.sparta.common.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
@RequestMapping("/api/v1/product-options")
@RequiredArgsConstructor
public class ProductOptionController {

    private final ProductOptionService productOptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER') or @authService.isProductOwner(#request.productId)")
    public ResponseEntity<ApiResponse<ProductOptionResponse>> createProductOption(
            @RequestBody @Valid ProductOptionCreateRequest request) {
        ProductOptionDto resultDto = productOptionService.createProductOption(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ProductOptionResponse.from(resultDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductOptionResponse>>> getProductOptions(
            @PageableDefault(size = 10, sort = "displayOrder", direction = Sort.Direction.ASC) Pageable pageable) {

        Pageable validatedPageable = PageableUtil.validatePageSize(pageable);

        PageResponse<ProductOptionResponse> response = new PageResponse<>(
                productOptionService.getProductOptions(validatedPageable).map(ProductOptionResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{productOptionId}")
    public ResponseEntity<ApiResponse<ProductOptionResponse>> getProductOption(
            @PathVariable("productOptionId") UUID productOptionId) {
        ProductOptionDto resultDto = productOptionService.getProductOption(productOptionId);
        return ResponseEntity.ok(ApiResponse.success(ProductOptionResponse.from(resultDto)));
    }

    @PatchMapping("/{productOptionId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER') or @authService.isProductOwnerByOptionId(#productOptionId)")
    public ResponseEntity<ApiResponse<ProductOptionResponse>> updateProductOption(
            @PathVariable("productOptionId") UUID productOptionId,
            @RequestBody @Valid ProductOptionUpdateRequest request) {
        ProductOptionDto resultDto = productOptionService.updateProductOption(productOptionId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(ProductOptionResponse.from(resultDto)));
    }

    @DeleteMapping("/{productOptionId}")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER') or @authService.isProductOwnerByOptionId(#productOptionId)")
    public ResponseEntity<ApiResponse<ProductOptionDeleteResponse>> deleteProductOption(
            @PathVariable UUID productOptionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUserId());
        ProductOptionDto resultDto = productOptionService.deleteProductOption(productOptionId, userId);
        return ResponseEntity.ok(ApiResponse.success(ProductOptionDeleteResponse.of(resultDto.getProductOptionId(), resultDto.getDeletedAt())));
    }
}
