package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductCategoryUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductCategoryUpdateRequest {

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String name;

    @NotNull(message = "카테고리 깊이는 필수입니다.")
    private Integer depth;

    public ProductCategoryUpdateCommand toCommand() {
        return ProductCategoryUpdateCommand.builder()
                .name(this.name)
                .depth(this.depth)
                .build();
    }
}
