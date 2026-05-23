package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductOptionCreateCommand;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductOptionCreateRequest {

    @NotNull(message = "상품 ID는 필수입니다.")
    private UUID productId;

    @NotBlank(message = "옵션 명칭은 필수입니다.")
    private String optionsName;

    @NotNull(message = "추가 가격은 필수입니다.")
    private BigDecimal extraPrice;

    private ProductStatusEnum status;

    private Integer displayOrder;

    public ProductOptionCreateCommand toCommand() {
        return ProductOptionCreateCommand.builder()
                .productId(this.productId)
                .optionsName(this.optionsName)
                .extraPrice(this.extraPrice)
                .status(this.status != null ? this.status : ProductStatusEnum.ON_SALE)
                .displayOrder(this.displayOrder)
                .build();
    }
}
