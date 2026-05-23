package com.sparta.companyservice.product.application.dto;

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
public class ProductOptionCreateCommand {
    private UUID productId;
    private String optionsName;
    private BigDecimal extraPrice;
    private String status;
    private Integer displayOrder;
}
