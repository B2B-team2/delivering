package com.sparta.companyservice.product.presentation.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductOptionDetailsRequest {
    @NotEmpty(message = "조회할 옵션 ID 목록이 비어있습니다.")
    private List<UUID> productOptionIds;
}
