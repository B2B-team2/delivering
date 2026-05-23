package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductOptionUpdateCommand;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ProductOptionUpdateRequest {

    @NotBlank(message = "옵션 명칭은 필수입니다.")
    private String optionsName;

    @NotNull(message = "추가 가격은 필수입니다.")
    private BigDecimal extraPrice;

    @NotNull(message = "상태값은 필수입니다.")
    private ProductStatusEnum status;

    private Integer displayOrder;

    public ProductOptionUpdateCommand toCommand() {
        return ProductOptionUpdateCommand.builder()
                .optionsName(this.optionsName)
                .extraPrice(this.extraPrice)
                .status(this.status)
                .displayOrder(this.displayOrder)
                .build();
    }
}
