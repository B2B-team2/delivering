package com.sparta.companyservice.product.domain.core;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "p_product_options")
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProductOption extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID productOptionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 255)
    private String optionsName;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal extraPrice = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    @Builder.Default
    private ProductStatusEnum status = ProductStatusEnum.ON_SALE;

    private Integer displayOrder;

    public void update(String optionsName, BigDecimal extraPrice, ProductStatusEnum status, Integer displayOrder) {
        this.optionsName = optionsName;
        this.extraPrice = extraPrice;
        this.status = status;
        this.displayOrder = displayOrder;
    }

}
