package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductDto;
import com.sparta.companyservice.product.application.dto.ProductUpdateCommand;
import com.sparta.companyservice.product.application.port.CompanyQueryPort;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductCategory;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.domain.repository.ProductCategoryRepository;
import com.sparta.companyservice.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCategoryRepository categoryRepository;

    @Mock
    private CompanyQueryPort companyQueryPort;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("상품 생성 성공")
    void createProductSuccessTest() {
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

        when(companyQueryPort.existsCompanyById(command.getCompanyId())).thenReturn(true);
        when(categoryRepository.findById(command.getCategoryId())).thenReturn(Optional.of(ProductCategory.builder().build()));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        ProductDto result = productService.createProduct(command);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(command.getName());
        assertThat(result.getStatus()).isEqualTo(ProductStatusEnum.ON_SALE.name());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("상품 생성 실패: 존재하지 않는 업체")
    void createProductFail_CompanyNotFound() {
        ProductCreateCommand command = ProductCreateCommand.builder()
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .build();

        when(companyQueryPort.existsCompanyById(command.getCompanyId())).thenReturn(false);

        assertThatThrownBy(() -> productService.createProduct(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.COMPANY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상품 생성 실패: 존재하지 않는 카테고리")
    void createProductFail_CategoryNotFound() {
        ProductCreateCommand command = ProductCreateCommand.builder()
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .build();

        when(companyQueryPort.existsCompanyById(command.getCompanyId())).thenReturn(true);
        when(categoryRepository.findById(command.getCategoryId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상품 상세 조회 성공")
    void getProductSuccessTest() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .productId(productId)
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .status(ProductStatusEnum.ON_SALE)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDto result = productService.getProduct(productId);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productId);
        assertThat(result.getName()).isEqualTo("테스트 상품");
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("상품 상세 조회 실패: 없는 상품")
    void getProductFailTest() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상품 목록 페이징 조회 성공")
    void getProductsSuccessTest() {
        Pageable pageable = PageRequest.of(0, 10);
        Product product = Product.builder()
                .productId(UUID.randomUUID())
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .status(ProductStatusEnum.ON_SALE)
                .build();

        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        Page<ProductDto> result = productService.getProducts(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("테스트 상품");
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("상품 수정 성공")
    void updateProductSuccessTest() {
        UUID productId = UUID.randomUUID();
        Product existingProduct = Product.builder()
                .productId(productId)
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("기존 상품")
                .price(BigDecimal.valueOf(10000))
                .status(ProductStatusEnum.ON_SALE)
                .build();

        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .companyId(existingProduct.getCompanyId())
                .categoryId(existingProduct.getCategoryId())
                .name("변경된 상품")
                .price(BigDecimal.valueOf(20000))
                .status("SOLD_OUT")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(companyQueryPort.existsCompanyById(command.getCompanyId())).thenReturn(true);
        when(categoryRepository.findById(command.getCategoryId())).thenReturn(Optional.of(ProductCategory.builder().build()));

        ProductDto result = productService.updateProduct(productId, command);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("변경된 상품");
        assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(20000));
        assertThat(result.getStatus()).isEqualTo("SOLD_OUT");
    }

    @Test
    @DisplayName("상품 수정 실패: 존재하지 않는 상품")
    void updateProductFail_NotFound() {
        UUID productId = UUID.randomUUID();
        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .name("변경된 상품")
                .status("ON_SALE")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(productId, command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("상품 수정 실패: 유효하지 않은 상품 상태 Enum")
    void updateProductFail_InvalidStatus() {
        UUID productId = UUID.randomUUID();
        Product existingProduct = Product.builder()
                .productId(productId)
                .status(ProductStatusEnum.ON_SALE)
                .build();

        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .companyId(UUID.randomUUID())
                .status("INVALID_STATUS")
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(existingProduct));
        when(companyQueryPort.existsCompanyById(command.getCompanyId())).thenReturn(true);

        assertThatThrownBy(() -> productService.updateProduct(productId, command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.INVALID_PRODUCT_STATUS.getMessage());
    }

    @Test
    @DisplayName("상품 삭제 성공")
    void deleteProductSuccessTest() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .productId(productId)
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .status(ProductStatusEnum.ON_SALE)
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        ProductDto result = productService.deleteProduct(productId);

        assertThat(result).isNotNull();
        assertThat(product.getDeletedAt()).isNotNull();
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("상품 삭제 실패: 존재하지 않는 상품")
    void deleteProductFailTest() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(productId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }
}
