package com.sparta.companyservice.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.handler.GlobalExceptionHandler;
import com.sparta.companyservice.product.application.dto.ProductCreateCommand;
import com.sparta.companyservice.product.application.dto.ProductDto;
import com.sparta.companyservice.product.application.service.ProductService;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.presentation.dto.ProductCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser
    @DisplayName("상품 생성 API 성공 검증")
    void createProductSuccessTest() throws Exception {
        // given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .companyId(UUID.randomUUID())
                .categoryId(UUID.randomUUID())
                .name("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .description("테스트 설명")
                .thumbnailUrl("http://test.com/image.png")
                .build();

        ProductDto responseDto = ProductDto.builder()
                .productId(UUID.randomUUID())
                .companyId(request.getCompanyId())
                .categoryId(request.getCategoryId())
                .name(request.getName())
                .price(request.getPrice())
                .status(ProductStatusEnum.ON_SALE.name())
                .build();

        when(productService.createProduct(any(ProductCreateCommand.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.name").value(request.getName()));
    }

    @Test
    @WithMockUser
    @DisplayName("상품 생성 API 실패 검증: 필수 파라미터 누락")
    void createProductFailTest() throws Exception {
        // given
        ProductCreateRequest request = ProductCreateRequest.builder()
                .name("") // NotBlank 위반
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
