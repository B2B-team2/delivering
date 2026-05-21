package com.sparta.companyservice.product.infrastructure.repository;

import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.infrastructure.repository.CompanyJpaRepository;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.global.config.JpaConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JpaConfig.class)
class ProductJpaRepositoryTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private CompanyJpaRepository companyJpaRepository;

    @Test
    @DisplayName("findAllByDeletedAtIsNull 페이징 조회 성공")
    void findAllByDeletedAtIsNullTest() {
        // given
        Company company = Company.builder()
                .companyName("테스트 업체")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber(UUID.randomUUID().toString().substring(0, 10))
                .hubId(UUID.randomUUID())
                .latitude(37.0)
                .longitude(127.0)
                .build();
        company = companyJpaRepository.save(company);

        Product product1 = Product.builder()
                .companyId(company.getCompanyId())
                .categoryId(null)
                .name("상품1")
                .price(BigDecimal.valueOf(1000))
                .status(ProductStatusEnum.ON_SALE)
                .build();
        Product product2 = Product.builder()
                .companyId(company.getCompanyId())
                .categoryId(null)
                .name("상품2")
                .price(BigDecimal.valueOf(2000))
                .status(ProductStatusEnum.ON_SALE)
                .build();
        productJpaRepository.save(product1);
        productJpaRepository.save(product2);

        // deleted product
        Product deletedProduct = Product.builder()
                .companyId(company.getCompanyId())
                .categoryId(null)
                .name("삭제된 상품")
                .price(BigDecimal.valueOf(3000))
                .status(ProductStatusEnum.ON_SALE)
                .build();
        deletedProduct.softDelete("system");
        productJpaRepository.save(deletedProduct);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Product> result = productJpaRepository.findAllByDeletedAtIsNull(pageable);

        // then
        assertThat(result.getContent()).extracting("name")
                .contains("상품1", "상품2");
        assertThat(result.getContent()).extracting("name")
                .doesNotContain("삭제된 상품");
    }
}
