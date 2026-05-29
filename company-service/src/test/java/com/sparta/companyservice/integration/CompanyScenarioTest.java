package com.sparta.companyservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.companyservice.company.presentation.dto.CompanyAddressCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyCreateRequest;
import com.sparta.companyservice.company.presentation.dto.CompanyUpdateRequest;
import com.sparta.companyservice.integration.support.IntegrationTestSupport;
import com.sparta.companyservice.product.presentation.dto.ProductCreateRequest;
import com.sparta.companyservice.product.presentation.dto.ProductOptionCreateRequest;
import com.sparta.companyservice.product.presentation.dto.ProductStatusUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class CompanyScenarioTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID createTestCompany(String bizNum) throws Exception {
        CompanyCreateRequest createRequest = CompanyCreateRequest.builder()
                .companyName("테스트 업체")
                .companyType("PRODUCER")
                .businessNumber(bizNum)
                .hubId(UUID.randomUUID())
                .latitude(37.5)
                .longitude(127.0)
                .build();

        String response = mockMvc.perform(post("/api/v1/companies")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).get("data").get("companyId").asText());
    }

    @Test
    @DisplayName("시나리오 1: 업체 관리 핵심 흐름 (등록 -> 수정 -> 조회)")
    void companyManagementScenario() throws Exception {
        // [1] 업체 등록
        UUID companyId = createTestCompany("INTEG-TEST-001");

        // [2] 업체 정보 수정
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
                .andExpect(status().isOk());

        // [3] 최종 정보 조회
        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.companyName").value("수정된 통합테스트 업체"))
                .andExpect(jsonPath("$.data.companyType").value("RECEIVER"));
    }

    @Test
    @DisplayName("시나리오 2 & 3: 상품 등록, 상세 조회 및 판매 상태 변경 흐름")
    void productManagementScenario() throws Exception {
        UUID companyId = createTestCompany("PROD-TEST-001");

        // [1] 상품 등록
        ProductCreateRequest createRequest = ProductCreateRequest.builder()
                .companyId(companyId)
                .name("시나리오 상품")
                .price(new BigDecimal("10000"))
                .description("상품 설명")
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/products")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID productId = UUID.fromString(objectMapper.readTree(createResponse).get("data").get("productId").asText());

        // [2] 상품 상세 조회
        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("시나리오 상품"));

        // [3] 판매 상태 변경 (판매 중단)
        String statusJson = "{\"status\":\"DISCONTINUED\"}";

        mockMvc.perform(patch("/api/v1/products/{productId}/status", productId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(statusJson))
                .andExpect(status().isOk());

        // [4] 상태 변경 후 조회
        mockMvc.perform(get("/api/v1/products/{productId}", productId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISCONTINUED"));
    }

    @Test
    @DisplayName("시나리오 4: 상품 옵션 생성 및 관리 흐름")
    void productOptionScenario() throws Exception {
        UUID companyId = createTestCompany("OPT-TEST-001");
        
        // 사전 상품 생성
        ProductCreateRequest pRequest = ProductCreateRequest.builder()
                .companyId(companyId)
                .name("옵션 테스트 상품")
                .price(new BigDecimal("5000"))
                .build();
        String pResponse = mockMvc.perform(post("/api/v1/products")
                .header("X-User-Id", UUID.randomUUID().toString())
                .header("X-User-Role", "MASTER")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pRequest))).andReturn().getResponse().getContentAsString();
        UUID productId = UUID.fromString(objectMapper.readTree(pResponse).get("data").get("productId").asText());

        // [1] 옵션 생성
        ProductOptionCreateRequest optionRequest = ProductOptionCreateRequest.builder()
                .productId(productId)
                .optionsName("빨간색")
                .extraPrice(new BigDecimal("500"))
                .displayOrder(1)
                .build();

        String oResponse = mockMvc.perform(post("/api/v1/product-options")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(optionRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        UUID optionId = UUID.fromString(objectMapper.readTree(oResponse).get("data").get("productOptionId").asText());

        // [2] 옵션 상세 조회
        mockMvc.perform(get("/api/v1/product-options/{optionId}", optionId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.optionsName").value("빨간색"));
    }

    @Test
    @DisplayName("시나리오 5: 업체 삭제 및 소프트 딜리트 검증 흐름")
    void companySoftDeleteScenario() throws Exception {
        UUID companyId = createTestCompany("DEL-TEST-002");

        // [2] 업체 삭제
        mockMvc.perform(delete("/api/v1/companies/{companyId}", companyId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());

        // [3] 삭제된 업체 조회 (404 기대)
        mockMvc.perform(get("/api/v1/companies/{companyId}", companyId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("시나리오 6: 업체 관리자(COMPANY_MANAGER)의 자기 업체 정보 수정 권한 검증")
    void companyManagerAuthorityScenario() throws Exception {
        // [1] 업체 등록
        String bizNum = "AUTH-TEST-002";
        UUID companyId = createTestCompany(bizNum);
        UUID managerId = UUID.randomUUID();

        // [2] 자기 업체 수정 (성공)
        CompanyUpdateRequest updateRequest = CompanyUpdateRequest.builder()
                .companyName("수정 성공")
                .companyType("PRODUCER")
                .businessNumber(bizNum)
                .hubId(UUID.randomUUID())
                .latitude(37.5)
                .longitude(127.0)
                .build();

        mockMvc.perform(patch("/api/v1/companies/{companyId}", companyId)
                        .header("X-User-Id", managerId.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        // [3] 타 업체 수정 (실패 - 403)
        UUID otherCompanyId = UUID.randomUUID();
        mockMvc.perform(patch("/api/v1/companies/{companyId}", otherCompanyId)
                        .header("X-User-Id", managerId.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("시나리오 7: 배송지 등록 및 기본 배송지 설정 흐름")
    void companyAddressScenario() throws Exception {
        UUID companyId = createTestCompany("ADDR-TEST-001");
        UUID managerId = UUID.randomUUID();

        // [1] 배송지 1 등록
        CompanyAddressCreateRequest addr1 = CompanyAddressCreateRequest.builder()
                .addressName("배송지1")
                .recipientName("수령인1")
                .phone("010-1111-1111")
                .address("서울시 강남구")
                .isDefault(false)
                .build();

        String res1 = mockMvc.perform(post("/api/v1/companies/{companyId}/addresses", companyId)
                        .header("X-User-Id", managerId.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addr1)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID addrId1 = UUID.fromString(objectMapper.readTree(res1).get("data").get("addressId").asText());

        // [2] 배송지 2 등록 (기본으로 설정)
        CompanyAddressCreateRequest addr2 = CompanyAddressCreateRequest.builder()
                .addressName("배송지2")
                .recipientName("수령인2")
                .phone("010-2222-2222")
                .address("서울시 서초구")
                .isDefault(true)
                .build();

        String res2 = mockMvc.perform(post("/api/v1/companies/{companyId}/addresses", companyId)
                        .header("X-User-Id", managerId.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", companyId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addr2)))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        UUID addrId2 = UUID.fromString(objectMapper.readTree(res2).get("data").get("addressId").asText());

        // [3] 배송지 목록 조회하여 기본 배송지 설정 확인
        mockMvc.perform(get("/api/v1/companies/{companyId}/addresses", companyId)
                        .header("X-User-Id", managerId.toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .header("X-Company-Id", companyId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[?(@.isDefault == true)].addressId").value(addrId2.toString()));
    }
}
