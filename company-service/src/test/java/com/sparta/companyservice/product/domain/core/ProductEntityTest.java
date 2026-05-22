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

    @Test
    @DisplayName("update() 메서드를 통해 엔티티 필드가 정상적으로 변경된다.")
    void update_Method_Changes_Fields_Correctly() {
        // given
        Product product = Product.builder()
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("기존 상품명")
                .price(BigDecimal.valueOf(1000))
                .description("기존 설명")
                .thumbnailUrl("http://old.url")
                .status(ProductStatusEnum.ON_SALE)
                .build();

        UUID newCompanyId = UUID.randomUUID();
        UUID newCategoryId = UUID.randomUUID();
        String newName = "변경된 상품명";
        BigDecimal newPrice = BigDecimal.valueOf(2000);
        String newDesc = "변경된 설명";
        String newThumb = "http://new.url";
        ProductStatusEnum newStatus = ProductStatusEnum.SOLD_OUT;

        // when
        product.update(newCompanyId, newCategoryId, newName, newPrice, newDesc, newThumb, newStatus);

        // then
        assertThat(product.getCompanyId()).isEqualTo(newCompanyId);
        assertThat(product.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(product.getName()).isEqualTo(newName);
        assertThat(product.getPrice()).isEqualTo(newPrice);
        assertThat(product.getDescription()).isEqualTo(newDesc);
        assertThat(product.getThumbnailUrl()).isEqualTo(newThumb);
        assertThat(product.getStatus()).isEqualTo(newStatus);
    }

    @Test
    @DisplayName("updateStatus() 메서드를 통해 상품 상태만 변경된다")
    void updateStatus_Method_Changes_Only_Status_Correctly() {
        // given
        UUID companyId = UUID.randomUUID();
        Product product = Product.builder()
                .companyId(companyId)
                .categoryId(UUID.randomUUID())
                .name("상품명")
                .price(BigDecimal.valueOf(1000))
                .description("설명")
                .thumbnailUrl("http://url")
                .status(ProductStatusEnum.ON_SALE)
                .build();

        // when
        product.updateStatus(ProductStatusEnum.SOLD_OUT);

        // then
        assertThat(product.getStatus()).isEqualTo(ProductStatusEnum.SOLD_OUT);
        assertThat(product.getCompanyId()).isEqualTo(companyId);
        assertThat(product.getName()).isEqualTo("상품명");
    }

}
