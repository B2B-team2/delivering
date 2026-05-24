package com.sparta.companyservice.product.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.common.util.PageableUtil;
import com.sparta.companyservice.product.application.dto.ProductDto;
import com.sparta.companyservice.product.application.service.ProductService;
import com.sparta.companyservice.product.presentation.dto.ProductCreateRequest;
import com.sparta.companyservice.product.presentation.dto.ProductDeleteResponse;
import com.sparta.companyservice.product.presentation.dto.ProductResponse;
import com.sparta.companyservice.product.presentation.dto.ProductStatusUpdateRequest;
import com.sparta.companyservice.product.presentation.dto.ProductStatusUpdateResponse;
import com.sparta.companyservice.product.presentation.dto.ProductUpdateRequest;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody @Valid ProductCreateRequest request) {
        ProductDto resultDto = productService.createProduct(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ProductResponse.from(resultDto)));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable UUID productId) {
        ProductDto resultDto = productService.getProduct(productId);
        return ResponseEntity.ok(ApiResponse.success(ProductResponse.from(resultDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProducts(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        
        Pageable validatedPageable = PageableUtil.validatePageSize(pageable);
        
        PageResponse<ProductResponse> response = new PageResponse<>(
                productService.getProducts(validatedPageable).map(ProductResponse::from)
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductResponse>> patchProduct(
            @PathVariable UUID productId,
            @RequestBody @Valid ProductUpdateRequest request) {
        ProductDto resultDto = productService.updateProduct(productId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(ProductResponse.from(resultDto)));
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductStatusUpdateResponse>> patchProductStatus(
            @PathVariable UUID productId,
            @RequestBody @Valid ProductStatusUpdateRequest request) {
        ProductDto resultDto = productService.updateProductStatus(productId, request.toCommand());
        return ResponseEntity.ok(ApiResponse.success(ProductStatusUpdateResponse.from(resultDto)));
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductDeleteResponse>> deleteProduct(
            @PathVariable UUID productId) {
        // TODO: 추후 인증/인가 로직 도입 시 실제 사용자 ID로 교체 필요
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        ProductDto resultDto = productService.deleteProduct(productId, userId);
        return ResponseEntity.ok(ApiResponse.success(ProductDeleteResponse.from(resultDto.getProductId(), resultDto.getDeletedAt())));
    }
}
