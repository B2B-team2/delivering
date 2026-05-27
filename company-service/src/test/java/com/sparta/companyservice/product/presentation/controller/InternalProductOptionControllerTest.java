package com.sparta.companyservice.product.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.handler.GlobalExceptionHandler;
import com.sparta.companyservice.product.application.dto.ProductOptionDetailDto;
import com.sparta.companyservice.product.application.service.ProductOptionService;
import com.sparta.companyservice.product.presentation.dto.ProductOptionDetailsRequest;
import com.sparta.companyservice.product.presentation.dto.ProductOptionDetailsResponse;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InternalProductOptionController.class)
@Import(GlobalExceptionHandler.class)
class InternalProductOptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductOptionService productOptionService;

    @Test
    @WithMockUser
    @DisplayName("Internal API 응답 규격 검증: POST /api/v1/internal/product-options/details 호출 시 Map 구조 데이터 반환")
    void getProductOptionDetailsApiResponseFormatTest() throws Exception {
        // given
        UUID optionId = UUID.randomUUID();
        ProductOptionDetailsRequest request = ProductOptionDetailsRequest.builder()
                .productOptionIds(List.of(optionId))
                .build();

        ProductOptionDetailDto dto = ProductOptionDetailDto.builder()
                .productOptionId(optionId)
                .companyId(UUID.randomUUID())
                .unitPrice(BigDecimal.valueOf(15000))
                .build();

        when(productOptionService.getProductOptionDetails(anyList())).thenReturn(Map.of(optionId, dto));

        // when & then
        mockMvc.perform(post("/api/v1/internal/product-options/details")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.optionsMap['" + optionId + "'].unitPrice").value(15000));
    }
}
