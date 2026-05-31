package com.sparta.companyservice.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.handler.GlobalExceptionHandler;
import com.sparta.companyservice.product.application.dto.ProductOptionDto;
import com.sparta.companyservice.product.application.service.ProductOptionService;
import com.sparta.companyservice.product.domain.core.ProductStatusEnum;
import com.sparta.companyservice.product.presentation.dto.ProductOptionCreateRequest;
import com.sparta.companyservice.product.presentation.dto.ProductOptionUpdateRequest;
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
import org.springframework.core.MethodParameter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import com.sparta.common.security.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductOptionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductOptionService productOptionService;

    @InjectMocks
    private ProductOptionController productOptionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        CustomUserDetails userDetails = new CustomUserDetails(UUID.randomUUID().toString(), "MASTER", null, Collections.emptyList());
        
        mockMvc = MockMvcBuilders.standaloneSetup(productOptionController)
                .setCustomArgumentResolvers(
                        new PageableHandlerMethodArgumentResolver(),
                        new HandlerMethodArgumentResolver() {
                            @Override
                            public boolean supportsParameter(MethodParameter parameter) {
                                return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                            }

                            @Override
                            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                          NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                                return userDetails;
                            }
                        })
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("상품 옵션 등록 API 성공 검증")
    void createProductOptionSuccessTest() throws Exception {
        // given
        UUID productId = UUID.randomUUID();
        ProductOptionCreateRequest request = ProductOptionCreateRequest.builder()
                .productId(productId)
                .optionsName("테스트 옵션")
                .extraPrice(new BigDecimal("10000"))
                .status(ProductStatusEnum.ON_SALE.name())
                .displayOrder(1)
                .build();

        ProductOptionDto dto = ProductOptionDto.builder()
                .productOptionId(UUID.randomUUID())
                .productId(productId)
                .optionsName("테스트 옵션")
                .extraPrice(new BigDecimal("10000"))
                .status(ProductStatusEnum.ON_SALE.name())
                .displayOrder(1)
                .build();

        when(productOptionService.createProductOption(any())).thenReturn(dto);

        // when & then
        mockMvc.perform(post("/api/v1/product-options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.optionsName").value("테스트 옵션"));
    }

    @Test
    @DisplayName("상품 옵션 목록 조회 API 성공 검증")
    void getProductOptionsSuccessTest() throws Exception {
        // given
        ProductOptionDto dto = ProductOptionDto.builder()
                .productOptionId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .optionsName("목록 테스트")
                .status(ProductStatusEnum.ON_SALE.name())
                .displayOrder(1)
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("displayOrder").ascending());

        when(productOptionService.getProductOptions(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(dto), pageable, 1));

        // when & then
        mockMvc.perform(get("/api/v1/product-options")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].optionsName").value("목록 테스트"));
    }

    @Test
    @DisplayName("상품 옵션 상세 조회 API 성공 검증")
    void getProductOptionSuccessTest() throws Exception {
        // given
        UUID optionId = UUID.randomUUID();
        ProductOptionDto dto = ProductOptionDto.builder()
                .productOptionId(optionId)
                .productId(UUID.randomUUID())
                .optionsName("상세 조회")
                .status(ProductStatusEnum.ON_SALE.name())
                .displayOrder(1)
                .build();

        when(productOptionService.getProductOption(optionId)).thenReturn(dto);

        // when & then
        mockMvc.perform(get("/api/v1/product-options/{productOptionId}", optionId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.optionsName").value("상세 조회"));
    }

    @Test
    @DisplayName("상품 옵션 수정 API 성공 검증")
    void updateProductOptionSuccessTest() throws Exception {
        // given
        UUID optionId = UUID.randomUUID();
        ProductOptionUpdateRequest request = ProductOptionUpdateRequest.builder()
                .optionsName("수정된 이름")
                .extraPrice(new BigDecimal("20000"))
                .status(ProductStatusEnum.SOLD_OUT.name())
                .displayOrder(2)
                .build();

        ProductOptionDto dto = ProductOptionDto.builder()
                .productOptionId(optionId)
                .productId(UUID.randomUUID())
                .optionsName("수정된 이름")
                .extraPrice(new BigDecimal("20000"))
                .status(ProductStatusEnum.SOLD_OUT.name())
                .displayOrder(2)
                .build();

        when(productOptionService.updateProductOption(eq(optionId), any())).thenReturn(dto);

        // when & then
        mockMvc.perform(patch("/api/v1/product-options/{productOptionId}", optionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.optionsName").value("수정된 이름"));
    }

    @Test
    @DisplayName("상품 옵션 삭제 API 성공 검증")
    void deleteProductOptionSuccessTest() throws Exception {
        // given
        UUID optionId = UUID.randomUUID();
        ProductOptionDto dto = ProductOptionDto.builder()
                .productOptionId(optionId)
                .deletedAt(java.time.LocalDateTime.now())
                .build();

        when(productOptionService.deleteProductOption(eq(optionId), any())).thenReturn(dto);

        // when & then
        CustomUserDetails userDetails = new CustomUserDetails(UUID.randomUUID().toString(), "MASTER", null, Collections.emptyList());
        UsernamePasswordAuthenticationToken principal = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        mockMvc.perform(delete("/api/v1/product-options/{productOptionId}", optionId)
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.productOptionId").value(optionId.toString()));
    }
}
