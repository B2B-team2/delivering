package com.sparta.companyservice.product.presentation.dto;

import com.sparta.companyservice.product.application.dto.ProductStatusUpdateCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductStatusUpdateRequest {

    @NotBlank(message = "상품 상태는 필수입니다.")
    private String status;

    public ProductStatusUpdateCommand toCommand() {
        return new ProductStatusUpdateCommand(this.status);
    }
}
