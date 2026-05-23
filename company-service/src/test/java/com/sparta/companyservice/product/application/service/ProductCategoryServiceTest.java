package com.sparta.companyservice.product.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import com.sparta.companyservice.product.application.dto.ProductCategoryCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductCategoryDto;
import com.sparta.companyservice.product.application.dto.ProductCategoryUpdateCommand;
import com.sparta.companyservice.product.domain.core.ProductCategory;
import com.sparta.companyservice.product.domain.repository.ProductCategoryRepository;
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
class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryRepository categoryRepository;

    @InjectMocks
    private ProductCategoryService categoryService;

    @Test
    @DisplayName("카테고리 등록 성공")
    void createCategorySuccessTest() {
        // given
        ProductCategoryCreateCommand command = ProductCategoryCreateCommand.builder()
                .name("가전제품")
                .depth(1)
                .build();

        ProductCategory savedCategory = ProductCategory.builder()
                .categoryId(UUID.randomUUID())
                .name(command.getName())
                .depth(command.getDepth())
                .build();

        when(categoryRepository.save(any(ProductCategory.class))).thenReturn(savedCategory);

        // when
        ProductCategoryDto result = categoryService.createCategory(command);

        // then
        assertThat(result.getName()).isEqualTo(command.getName());
        verify(categoryRepository, times(1)).existsByName(command.getName());
        verify(categoryRepository, times(1)).save(any(ProductCategory.class));
    }

    @Test
    @DisplayName("카테고리 등록 실패: 중복된 이름")
    void createCategoryFail_DuplicateName() {
        // given
        ProductCategoryCreateCommand command = ProductCategoryCreateCommand.builder()
                .name("중복이름")
                .build();

        when(categoryRepository.existsByName(command.getName())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(command))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.DUPLICATE_CATEGORY_NAME.getMessage());
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategoriesSuccessTest() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        ProductCategory category = ProductCategory.builder()
                .categoryId(UUID.randomUUID())
                .name("의류")
                .build();

        when(categoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(category)));

        // when
        Page<ProductCategoryDto> result = categoryService.getCategories(pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("의류");
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void getCategorySuccessTest() {
        // given
        UUID categoryId = UUID.randomUUID();
        ProductCategory category = ProductCategory.builder()
                .categoryId(categoryId)
                .name("식품")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        ProductCategoryDto result = categoryService.getCategory(categoryId);

        // then
        assertThat(result.getCategoryId()).isEqualTo(categoryId);
        assertThat(result.getName()).isEqualTo("식품");
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategorySuccessTest() {
        // given
        UUID categoryId = UUID.randomUUID();
        ProductCategory category = ProductCategory.builder()
                .categoryId(categoryId)
                .name("구버전 이름")
                .depth(1)
                .build();

        ProductCategoryUpdateCommand command = ProductCategoryUpdateCommand.builder()
                .name("신버전 이름")
                .depth(2)
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        ProductCategoryDto result = categoryService.updateCategory(categoryId, command);

        // then
        assertThat(result.getName()).isEqualTo("신버전 이름");
        assertThat(result.getDepth()).isEqualTo(2);
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategorySuccessTest() {
        // given
        UUID categoryId = UUID.randomUUID();
        ProductCategory category = ProductCategory.builder()
                .categoryId(categoryId)
                .name("삭제할 카테고리")
                .build();

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // when
        ProductCategoryDto result = categoryService.deleteCategory(categoryId, "test-user");

        // then
        assertThat(result.getCategoryId()).isEqualTo(categoryId);
        verify(categoryRepository, times(1)).findById(categoryId);
        // Soft delete 필드는 BaseEntity에 있으므로 실제 값 검증보다는 예외 미발생 및 findById 호출 확인
    }

    @Test
    @DisplayName("카테고리 조회 실패: 존재하지 않는 ID")
    void getCategoryFail_NotFound() {
        // given
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(categoryId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(CompanyErrorCode.CATEGORY_NOT_FOUND.getMessage());
    }
}
