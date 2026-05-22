package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {

    @NotNull(message = "업체 ID는 필수입니다.")
    private UUID companyId;

    private UUID categoryId;

    @NotBlank(message = "상품명은 필수입니다.")
    private String name;

    @NotNull(message = "가격은 필수입니다.")
    @PositiveOrZero(message = "가격은 0원 이상이어야 합니다.")
    private BigDecimal price;

    private String description;

    private String thumbnailUrl;

    @NotBlank(message = "상품 상태는 필수입니다.")
    private String status;

    public ProductUpdateCommand toCommand() {
        return ProductUpdateCommand.builder()
                .companyId(this.companyId)
                .categoryId(this.categoryId)
                .name(this.name)
                .price(this.price)
                .description(this.description)
                .thumbnailUrl(this.thumbnailUrl)
                .status(this.status)
                .build();
    }
}
