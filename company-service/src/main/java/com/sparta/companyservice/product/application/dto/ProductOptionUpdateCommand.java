package com.sparta.companyservice.product.application.dto;

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
public class ProductOptionUpdateCommand {
    private String optionsName;
    private BigDecimal extraPrice;
    private String status;
    private Integer displayOrder;
}
