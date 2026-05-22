package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductStatusUpdateResponse {
    private UUID productId;
    private String status;

    public static ProductStatusUpdateResponse from(ProductDto dto) {
        return ProductStatusUpdateResponse.builder()
                .productId(dto.getProductId())
                .status(dto.getStatus())
                .build();
    }
}
