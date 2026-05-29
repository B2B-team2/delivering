package com.sparta.companyservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyUpdateRequest;
import com.sparta.companyservice.integration.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompanyScenarioTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("업체 관리 핵심 시나리오: 등록 -> 수정 -> 조회")
    void companyManagementScenario() throws Exception {
        // [1] 업체 등록 (MASTER 권한 모킹)
        // DummyInitializer와 겹치지 않는 고유한 사업자 번호 사용
        CompanyCreateRequest createRequest = CompanyCreateRequest.builder()
                .companyName("통합테스트 업체")
                .companyType("PRODUCER")
                .businessNumber("INTEG-TEST-001")
                .hubId(UUID.randomUUID())
                .latitude(37.5)
                .longitude(127.0)
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/companies")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyName").value("통합테스트 업체"))
                .andExpect(jsonPath("$.data.companyType").value("PRODUCER"))
                .andReturn().getResponse().getContentAsString();

        UUID companyId = UUID.fromString(objectMapper.readTree(createResponse).get("data").get("companyId").asText());

        // [2] 업체 정보 수정 (HUB_MANAGER 권한 모킹)
        CompanyUpdateRequest updateRequest = CompanyUpdateRequest.builder()
                .companyName("수정된 통합테스트 업체")
                .companyType("RECEIVER")
                .businessNumber("INTEG-TEST-001")
                .hubId(UUID.randomUUID())
                .latitude(37.6)
                .longitude(127.1)
                .build();

        mockMvc.perform(patch("/api/v1/companies/{companyId}", companyId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "HUB_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("수정된 통합테스트 업체"))
                .andExpect(jsonPath("$.data.companyType").value("RECEIVER"));

        // [3] 최종 정보 조회
        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(companyId.toString()))
                .andExpect(jsonPath("$.data.companyName").value("수정된 통합테스트 업체"))
                .andExpect(jsonPath("$.data.companyType").value("RECEIVER"));
    }
}
