package com.sparta.companyservice.product.domain.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductEntityTest {

    @Test
    @DisplayName("상품 엔티티 생성 검증")
    void createProductTest() {
        // given
        UUID companyId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        String name = "테스트 상품";
        BigDecimal price = BigDecimal.valueOf(10000);
        String description = "테스트 설명";
        String thumbnailUrl = "http://test.com/image.png";

        // when
        Product product = Product.builder()
                .companyId(companyId)
                .categoryId(categoryId)
                .name(name)
                .price(price)
                .description(description)
                .thumbnailUrl(thumbnailUrl)
                .status(ProductStatusEnum.ON_SALE)
                .build();

        // then
        assertThat(product.getCompanyId()).isEqualTo(companyId);
        assertThat(product.getCategoryId()).isEqualTo(categoryId);
        assertThat(product.getName()).isEqualTo(name);
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getDescription()).isEqualTo(description);
        assertThat(product.getThumbnailUrl()).isEqualTo(thumbnailUrl);
        assertThat(product.getStatus()).isEqualTo(ProductStatusEnum.ON_SALE);
    }

}
