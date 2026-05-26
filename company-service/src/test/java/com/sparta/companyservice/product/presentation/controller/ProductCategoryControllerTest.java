package com.sparta.companyservice.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.handler.GlobalExceptionHandler;
import com.sparta.companyservice.product.application.dto.ProductCategoryDto;
import com.sparta.companyservice.product.application.service.ProductCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductCategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductCategoryService categoryService;

    @InjectMocks
    private ProductCategoryController categoryController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("카테고리 목록 조회 API 성공 검증: 기본 정렬 조건 확인")
    void getCategoriesSuccessTest() throws Exception {
        // given
        ProductCategoryDto dto = ProductCategoryDto.builder()
                .categoryId(UUID.randomUUID())
                .name("전자제품")
                .depth(1)
                .build();

        // Controller에서 기대하는 기본 정렬: depth ASC, createdAt DESC
        Pageable expectedPageable = PageRequest.of(0, 10, Sort.by(
                Sort.Order.asc("depth"),
                Sort.Order.desc("createdAt")
        ));

        when(categoryService.getCategories(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), expectedPageable, 1));

        // when & then
        mockMvc.perform(get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content[0].name").value("전자제품"));
    }

    @Test
    @DisplayName("카테고리 상세 조회 API 성공 검증")
    void getCategorySuccessTest() throws Exception {
        // given
        UUID categoryId = UUID.randomUUID();
        ProductCategoryDto dto = ProductCategoryDto.builder()
                .categoryId(categoryId)
                .name("식품")
                .depth(1)
                .build();

        when(categoryService.getCategory(categoryId)).thenReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/categories/{categoryId}", categoryId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("식품"));
    }
}
