package com.sparta.companyservice.company.presentation.controller;

import com.sparta.companyservice.company.application.dto.CompanyDefaultAddressDto;
import com.sparta.companyservice.company.application.dto.CompanyHubMappingResult;
import com.sparta.companyservice.company.application.service.CompanyAddressService;
import com.sparta.companyservice.company.application.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalCompanyController.class)
@AutoConfigureMockMvc(addFilters = false)
class InternalCompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private CompanyAddressService companyAddressService;

    @Test
    @DisplayName("업체-허브 매핑 조회 성공: 200 OK와 정상 구조 응답 확인")
    void getHubMappingSuccess() throws Exception {
        // given
        UUID companyId = UUID.randomUUID();
        UUID hubId = UUID.randomUUID();
        CompanyHubMappingResult result = CompanyHubMappingResult.builder()
                .mappings(List.of(
                        CompanyHubMappingResult.MappingItem.builder()
                                .companyId(companyId)
                                .hubId(hubId)
                                .companyName("Test Company")
                                .build()
                ))
                .build();

        when(companyService.getHubMappings(anyList())).thenReturn(result);

        String requestBody = "{\"companyIds\": [\"" + companyId + "\"]}";

        // when & then
        mockMvc.perform(post("/api/v1/internal/companies/hub-mapping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mappings['" + companyId + "'].companyName").value("Test Company"))
                .andExpect(jsonPath("$.mappings['" + companyId + "'].hubId").value(hubId.toString()));
    }

    @Test
    @DisplayName("업체-허브 매핑 조회 실패: 빈 ID 목록 전달 시 400 Bad Request 확인")
    void getHubMappingFail_EmptyIds() throws Exception {
        // given
        String requestBody = "{\"companyIds\": []}";

        // when & then
        mockMvc.perform(post("/api/v1/internal/companies/hub-mapping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("허브 소속 업체 존재 확인 성공: 200 OK와 Boolean 결과 응답 확인")
    void existsCompanyInHubSuccess() throws Exception {
        // given
        UUID hubId = UUID.randomUUID();
        when(companyService.existsCompanyInHub(hubId)).thenReturn(true);

        // when & then
        mockMvc.perform(get("/api/v1/internal/companies/exists")
                        .param("hubId", hubId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("허브 소속 업체 존재 확인 실패: hubId 파라미터 누락 시 400 Bad Request 확인")
    void existsCompanyInHubFail_MissingParam() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/internal/companies/exists"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기본 배송지 조회 성공: 200 OK와 정상 데이터 응답 확인")
    void getDefaultAddressSuccess() throws Exception {
        // given
        UUID companyId = UUID.randomUUID();
        CompanyDefaultAddressDto responseDto = CompanyDefaultAddressDto.builder()
                .address("경기도 수원시")
                .addressDetail("3층")
                .recipientName("홍길동")
                .phone("010-1234-5678")
                .build();

        when(companyAddressService.getDefaultAddress(companyId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/internal/companies/{companyId}/default-address", companyId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("경기도 수원시"))
                .andExpect(jsonPath("$.recipientName").value("홍길동"));
    }
}
