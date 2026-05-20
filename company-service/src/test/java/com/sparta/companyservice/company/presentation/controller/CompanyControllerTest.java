package com.sparta.companyservice.company.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompanyService companyService;

    @Test
    @WithMockUser
    @DisplayName("API 응답 규격 검증: POST /api/v1/companies 호출 시 201 Created와 ApiResponse 포맷이 유지되는가?")
    void createCompanyApiResponseFormatTest() throws Exception {
        // given
        CompanyCreateRequest request = CompanyCreateRequest.builder()
                .companyName("Test Company")
                .companyType("HUB")
                .businessNumber("123-45-67890")
                .hubId(UUID.randomUUID())
                .latitude(37.5665)
                .longitude(126.9780)
                .build();

        CompanyDto responseDto = CompanyDto.builder()
                .companyId(UUID.randomUUID())
                .companyName(request.getCompanyName())
                .build();

        when(companyService.createCompany(any(CompanyCreateCommand.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.companyName").value(request.getCompanyName()));
    }

    @Test
    @WithMockUser
    @DisplayName("Bean Validation 검증: 필수 필드 누락 시 400 Bad Request를 반환하는가?")
    void createCompanyValidationTest() throws Exception {
        // given
        CompanyCreateRequest invalidRequest = CompanyCreateRequest.builder()
                .companyName("") // Blank
                .companyType("HUB")
                .hubId(null) // Null
                .build();

        // when & then
        mockMvc.perform(post("/api/v1/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("DTO 변환 검증: 요청 JSON이 CompanyCreateRequest 객체로 정확히 역직렬화되는가?")
    void requestDeserializationTest() throws Exception {
        // given
        String jsonRequest = "{\"companyName\":\"JSON Test\",\"companyType\":\"DELIVERY\",\"businessNumber\":\"111-22-33333\",\"hubId\":\"" + UUID.randomUUID() + "\",\"latitude\":35.0,\"longitude\":127.0}";

        when(companyService.createCompany(any())).thenReturn(CompanyDto.builder().build());

        // when & then
        mockMvc.perform(post("/api/v1/companies")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated());
    }
}
