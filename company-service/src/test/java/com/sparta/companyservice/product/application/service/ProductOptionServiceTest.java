package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductOptionCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductOptionDto;
import com.sparta.companyservice.product.application.dto.ProductOptionUpdateCommand;
import com.sparta.companyservice.product.domain.core.Product;
import com.sparta.companyservice.product.domain.core.ProductOption;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.domain.repository.ProductOptionRepository;
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
class ProductOptionServiceTest {

    @Mock
    private ProductOptionRepository productOptionRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductOptionService productOptionService;

    @Test
    @DisplayName("상품 옵션 등록 성공")
    void createProductOptionSuccessTest() {
        // given
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().productId(productId).build();
        ProductOptionCreateCommand command = ProductOptionCreateCommand.builder()
                .productId(productId)
                .optionsName("블랙/256GB")
                .extraPrice(new BigDecimal("150000"))
                .status(ProductStatusEnum.ON_SALE)
                .displayOrder(1)
                .build();

        ProductOption savedOption = ProductOption.builder()
                .productOptionId(UUID.randomUUID())
                .product(product)
                .optionsName(command.getOptionsName())
                .extraPrice(command.getExtraPrice())
                .status(command.getStatus())
                .displayOrder(command.getDisplayOrder())
                .build();

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productOptionRepository.save(any(ProductOption.class))).thenReturn(savedOption);

        // when
        ProductOptionDto result = productOptionService.createProductOption(command);

        // then
        assertThat(result.getOptionsName()).isEqualTo(command.getOptionsName());
        verify(productOptionRepository, times(1)).save(any(ProductOption.class));
    }

    @Test
    @DisplayName("상품 옵션 목록 조회 성공")
    void getProductOptionsSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        Product product = Product.builder().productId(UUID.randomUUID()).build();
        ProductOption option = ProductOption.builder()
                .productOptionId(UUID.randomUUID())
                .product(product)
                .optionsName("화이트")
                .build();

        when(productOptionRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(option)));

        // when
        Page<ProductOptionDto> result = productOptionService.getProductOptions(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getOptionsName()).isEqualTo("화이트");
    }

    @Test
    @DisplayName("상품 옵션 수정 성공")
    void updateProductOptionSuccessTest() {
        // given
        UUID optionId = UUID.randomUUID();
        Product product = Product.builder().productId(UUID.randomUUID()).build();
        ProductOption option = ProductOption.builder()
                .productOptionId(optionId)
                .product(product)
                .optionsName("기존 이름")
                .build();

        ProductOptionUpdateCommand command = ProductOptionUpdateCommand.builder()
                .optionsName("수정된 이름")
                .extraPrice(new BigDecimal("200000"))
                .status(ProductStatusEnum.SOLD_OUT)
                .displayOrder(5)
                .build();

        when(productOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        // when
        ProductOptionDto result = productOptionService.updateProductOption(optionId, command);

        // then
        assertThat(result.getOptionsName()).isEqualTo("수정된 이름");
        assertThat(result.getStatus()).isEqualTo(ProductStatusEnum.SOLD_OUT);
    }

    @Test
    @DisplayName("상품 옵션 삭제 성공")
    void deleteProductOptionSuccessTest() {
        // given
        UUID optionId = UUID.randomUUID();
        Product product = Product.builder().productId(UUID.randomUUID()).build();
        ProductOption option = ProductOption.builder()
                .productOptionId(optionId)
                .product(product)
                .build();

        when(productOptionRepository.findById(optionId)).thenReturn(Optional.of(option));

        // when
        ProductOptionDto result = productOptionService.deleteProductOption(optionId, "system");

        // then
        assertThat(result.getProductOptionId()).isEqualTo(optionId);
        verify(productOptionRepository, times(1)).findById(optionId);
    }

    @Test
    @DisplayName("상품 옵션 조회 실패: 존재하지 않는 ID")
    void getProductOptionFail_NotFound() {
        // given
        UUID optionId = UUID.randomUUID();
        when(productOptionRepository.findById(optionId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> productOptionService.getProductOption(optionId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.PRODUCT_OPTION_NOT_FOUND.getMessage());
    }
}
