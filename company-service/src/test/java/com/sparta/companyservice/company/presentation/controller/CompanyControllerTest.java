package com.sparta.companyservice.company.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.handler.GlobalExceptionHandler;
import com.sparta.companyservice.company.application.dto.CompanyCreateCommand;
import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.global.exception.CompanyErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@Import(GlobalExceptionHandler.class)
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

    @Test
    @WithMockUser
    @DisplayName("API 응답 규격 검증: GET /api/v1/companies 호출 시 200 OK와 PageResponse 포맷이 유지되는가?")
    void getCompaniesApiResponseFormatTest() throws Exception {
        // given
        CompanyDto responseDto = CompanyDto.builder()
                .companyId(UUID.randomUUID())
                .companyName("Test Company")
                .companyType("PRODUCER")
                .build();

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        when(companyService.getCompanies(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(responseDto), pageable, 1));

        // when & then
        mockMvc.perform(get("/api/v1/companies")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,DESC")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].companyName").value(responseDto.getCompanyName()))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("페이지네이션 사이즈 보정 검증: 허용되지 않은 사이즈(20) 요청 시 10으로 보정되어 서비스에 전달되는가?")
    void getCompaniesSizeCorrectionTest() throws Exception {
        // given
        when(companyService.getCompanies(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // when & then
        mockMvc.perform(get("/api/v1/companies")
                        .param("size", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // PageableUtil에 의해 size 20이 10으로 보정되어 서비스에 전달되었는지 확인
        verify(companyService).getCompanies(argThat(pageable -> pageable.getPageSize() == 10));
    }

    @Test
    @WithMockUser
    @DisplayName("업체 상세 조회 성공: GET /api/v1/companies/{companyId} 호출 시 200 OK와 상세 정보가 반환되는가?")
    void getCompanySuccessTest() throws Exception {
        // given
        UUID companyId = UUID.randomUUID();
        CompanyDto responseDto = CompanyDto.builder()
                .companyId(companyId)
                .companyName("Detail Test Company")
                .companyType("PRODUCER")
                .build();

        when(companyService.getCompany(companyId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("SUCCESS"))
                .andExpect(jsonPath("$.data.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.data.companyName").value(responseDto.getCompanyName()));
    }

    @Test
    @WithMockUser
    @DisplayName("업체 상세 조회 실패: 존재하지 않는 업체 조회 시 404 Not Found를 반환하는가?")
    void getCompanyNotFoundTest() throws Exception {
        // given
        UUID companyId = UUID.randomUUID();
        when(companyService.getCompany(companyId))
                .thenThrow(new com.sparta.common.dto.BusinessException(CompanyErrorCode.COMPANY_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("COMPANY_NOT_FOUND"));
    }
}
