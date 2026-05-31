package com.sparta.companyservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.companyservice.company.domain.core.Company;
import com.sparta.companyservice.company.domain.core.CompanyTypeEnum;
import com.sparta.companyservice.company.domain.repository.CompanyRepository;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyUpdateRequest;
import com.sparta.companyservice.integration.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class CompanyApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CompanyRepository companyRepository;

    private static final String GATEWAY_HEADER = "X-Gateway-Secret";
    private static final String GATEWAY_VALUE = "local-secret";

    @Test
    @DisplayName("POST /api/v1/companies - 업체 생성 API 통합 테스트")
    void createCompanyTest() throws Exception {
        // given
        String bizNum = "BIZ-" + UUID.randomUUID().toString().substring(0, 8);
        CompanyCreateRequest request = CompanyCreateRequest.builder()
                .companyName("API 통합 테스트 업체")
                .companyType("PRODUCER")
                .businessNumber(bizNum)
                .hubId(UUID.randomUUID())
                .latitude(37.5)
                .longitude(127.0)
                .build();

        // when
        mockMvc.perform(post("/api/v1/companies")
                        .header(GATEWAY_HEADER, GATEWAY_VALUE)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.companyName").value("API 통합 테스트 업체"));

        // then
        Company saved = companyRepository.findByBusinessNumberAnyStatus(bizNum).orElseThrow();
        assertThat(saved.getCompanyName()).isEqualTo("API 통합 테스트 업체");
    }

    @Test
    @DisplayName("GET /api/v1/companies/{companyId} - 업체 단건 조회 API 통합 테스트")
    void getCompanyTest() throws Exception {
        // given
        Company saved = companyRepository.save(Company.builder()
                .companyName("조회용 업체")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber("BIZ-GET-" + UUID.randomUUID().toString().substring(0, 5))
                .hubId(UUID.randomUUID())
                .latitude(37.5)
                .longitude(127.0)
                .build());

        // when & then
        mockMvc.perform(get("/api/v1/companies/{companyId}", saved.getCompanyId())
                        .header(GATEWAY_HEADER, GATEWAY_VALUE)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyId").value(saved.getCompanyId().toString()))
                .andExpect(jsonPath("$.data.companyName").value("조회용 업체"));
    }

    @Test
    @DisplayName("PATCH /api/v1/companies/{companyId} - 업체 수정 API 통합 테스트")
    void updateCompanyTest() throws Exception {
        // given
        Company saved = companyRepository.save(Company.builder()
                .companyName("수정 전 업체")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber("UPDATE-" + UUID.randomUUID().toString().substring(0, 8))
                .hubId(UUID.randomUUID())
                .latitude(37.1)
                .longitude(127.1)
                .build());

        CompanyUpdateRequest updateRequest = CompanyUpdateRequest.builder()
                .companyName("수정 후 업체")
                .companyType("RECEIVER")
                .businessNumber(saved.getBusinessNumber())
                .hubId(saved.getHubId())
                .latitude(37.2)
                .longitude(127.2)
                .build();

        // when
        mockMvc.perform(patch("/api/v1/companies/{companyId}", saved.getCompanyId())
                        .header(GATEWAY_HEADER, GATEWAY_VALUE)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // then
        Company updated = companyRepository.findById(saved.getCompanyId()).orElseThrow();
        assertThat(updated.getCompanyName()).isEqualTo("수정 후 업체");
    }

    @Test
    @DisplayName("DELETE /api/v1/companies/{companyId} - 업체 삭제(Soft Delete) API 통합 테스트")
    void deleteCompanyTest() throws Exception {
        // given
        String bizNum = "DEL-" + UUID.randomUUID().toString().substring(0, 8);
        Company saved = companyRepository.save(Company.builder()
                .companyName("삭제용 업체")
                .companyType(CompanyTypeEnum.PRODUCER)
                .businessNumber(bizNum)
                .hubId(UUID.randomUUID())
                .latitude(37.5)
                .longitude(127.0)
                .build());

        // when
        mockMvc.perform(delete("/api/v1/companies/{companyId}", saved.getCompanyId())
                        .header(GATEWAY_HEADER, GATEWAY_VALUE)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());

        // then
        Company deleted = companyRepository.findByBusinessNumberAnyStatus(bizNum).orElseThrow();
        assertThat(deleted.getDeletedAt()).isNotNull();
    }
}
