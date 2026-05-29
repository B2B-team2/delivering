package com.sparta.operationsservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.operationsservice.claim.application.port.HubPort;
import com.sparta.operationsservice.claim.application.port.OrderPort;
import com.sparta.operationsservice.claim.domain.core.ClaimStatus;
import com.sparta.operationsservice.claim.domain.core.ClaimType;
import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import com.sparta.operationsservice.claim.infrastructure.repository.OrderClaimJpaRepository;
import com.sparta.operationsservice.claim.presentation.dto.ClaimCreateRequest;
import com.sparta.operationsservice.claim.presentation.dto.ClaimStatusUpdateRequest;
import com.sparta.operationsservice.integration.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderClaimRepository orderClaimRepository;

    @Autowired
    private OrderClaimJpaRepository orderClaimJpaRepository;

    @MockBean
    private OrderPort orderPort;

    @MockBean
    private HubPort hubPort;

    @BeforeEach
    void setUp() {
        orderClaimJpaRepository.deleteAll();
    }

    @Test
    @Transactional
    @DisplayName("POST /api/v1/claims - 클레임 생성 API 통합 테스트")
    void createClaimTest() throws Exception {
        // given
        UUID companyOrderId = UUID.randomUUID();
        ClaimCreateRequest request = ClaimCreateRequest.builder()
                .companyOrderId(companyOrderId)
                .claimType("RETURN")
                .reason("API 통합 테스트 반품 사유")
                .refundAmount(new BigDecimal("15000"))
                .build();

        // when
        mockMvc.perform(post("/api/v1/claims")
                        .header("X-Gateway-Secret", "local-secret").header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.reason").value("API 통합 테스트 반품 사유"));

        // then
        assertThat(orderClaimRepository.existsByCompanyOrderId(companyOrderId)).isTrue();
    }

    @Test
    @Transactional
    @DisplayName("GET /api/v1/claims/{claimId} - 클레임 단건 조회 API 통합 테스트")
    void getClaimTest() throws Exception {
        // given
        OrderClaim claim = OrderClaim.builder()
                .companyOrderId(UUID.randomUUID())
                .claimType(ClaimType.RETURN)
                .reason("단건 조회 테스트")
                .refundAmount(BigDecimal.TEN)
                .status(ClaimStatus.REQUESTED)
                .build();
        OrderClaim saved = orderClaimRepository.save(claim);

        // when & then
        mockMvc.perform(get("/api/v1/claims/{claimId}", saved.getClaimId())
                        .header("X-Gateway-Secret", "local-secret").header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimId").value(saved.getClaimId().toString()))
                .andExpect(jsonPath("$.data.reason").value("단건 조회 테스트"));
    }

    @Test
    // @Transactional 제거: updateClaimStatus의 PROPAGATION_NOT_SUPPORTED를 테스트하기 위해 실제 DB에 커밋된 데이터가 필요함
    @DisplayName("PATCH /api/v1/claims/{claimId}/status - 클레임 상태 변경 API 통합 테스트")
    void updateClaimStatusTest() throws Exception {
        // given
        OrderClaim claim = OrderClaim.builder()
                .companyOrderId(UUID.randomUUID())
                .claimType(ClaimType.RETURN)
                .reason("상태 변경 테스트")
                .status(ClaimStatus.REQUESTED)
                .build();
        // 실제 DB에 저장 (커밋)
        OrderClaim saved = orderClaimJpaRepository.saveAndFlush(claim);

        ClaimStatusUpdateRequest request = ClaimStatusUpdateRequest.builder()
                .status("PROCESSING")
                .refundAmount(new BigDecimal("20000"))
                .build();

        // 외부 포트 모킹
        when(orderPort.getCompanyOrderDetails(any())).thenReturn(
                new OrderPort.CompanyOrderDetails(UUID.randomUUID(), saved.getCompanyOrderId(), List.of())
        );

        // when
        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", saved.getClaimId())
                        .header("X-Gateway-Secret", "local-secret").header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // then
        OrderClaim updated = orderClaimRepository.findById(saved.getClaimId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ClaimStatus.PROCESSING);
    }
}
