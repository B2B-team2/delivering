package com.sparta.companyservice.product.application.service;

import com.sparta.companyservice.product.application.dto.ProductCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductDto;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 생성 성공")
    void createProductSuccessTest() {
        // given
        ProductCreateCommand command = ProductCreateCommand.builder()
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .description("테스트 설명")
                .thumbnailUrl("http://test.com/image.png")
                .build();

        Product savedProduct = Product.builder()
                .productId(UUID.randomUUID())
                .companyId(command.getCompanyId())
                .categoryId(command.getCategoryId())
                .name(command.getName())
                .price(command.getPrice())
                .description(command.getDescription())
                .thumbnailUrl(command.getThumbnailUrl())
                .status(ProductStatusEnum.ON_SALE)
                .build();

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // when
        ProductDto result = productService.createProduct(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(command.getName());
        assertThat(result.getStatus()).isEqualTo(ProductStatusEnum.ON_SALE.name());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}
