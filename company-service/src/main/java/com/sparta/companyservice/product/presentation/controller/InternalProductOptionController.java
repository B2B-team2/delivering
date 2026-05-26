package com.sparta.companyservice.product.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.companyservice.product.application.dto.ProductOptionDetailDto;
import com.sparta.companyservice.product.application.service.ProductOptionService;
import com.sparta.companyservice.product.presentation.dto.ProductOptionDetailsRequest;
import com.sparta.companyservice.product.presentation.dto.ProductOptionDetailsResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/internal/product-options")
@RequiredArgsConstructor
public class InternalProductOptionController {

    private final ProductOptionService productOptionService;

    @PostMapping("/details")
    public ApiResponse<ProductOptionDetailsResponse> getProductOptionDetails(
            @RequestBody @Valid ProductOptionDetailsRequest request) {
        Map<UUID, ProductOptionDetailDto> details = productOptionService.getProductOptionDetails(request.getProductOptionIds());
        ProductOptionDetailsResponse response = ProductOptionDetailsResponse.builder()
                .optionsMap(details)
                .build();
        return ApiResponse.success(response);
    }
}
