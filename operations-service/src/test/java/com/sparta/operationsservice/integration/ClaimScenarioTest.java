package com.sparta.operationsservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.operationsservice.claim.application.port.HubPort;
import com.sparta.operationsservice.claim.application.port.OrderPort;
import com.sparta.operationsservice.claim.domain.core.ClaimStatus;
import com.sparta.operationsservice.claim.domain.core.OrderClaim;
import com.sparta.operationsservice.claim.domain.repository.OrderClaimRepository;
import com.sparta.operationsservice.claim.infrastructure.repository.OrderClaimJpaRepository;
import com.sparta.operationsservice.claim.presentation.dto.ClaimCreateRequest;
import com.sparta.operationsservice.claim.presentation.dto.ClaimStatusUpdateRequest;
import com.sparta.operationsservice.integration.support.IntegrationTestSupport;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimScenarioTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderClaimRepository orderClaimRepository;

    @Autowired
    private OrderClaimJpaRepository orderClaimJpaRepository;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private OrderPort orderPort;

    @MockBean
    private HubPort hubPort;

    @BeforeEach
    void setUp() {
        orderClaimJpaRepository.deleteAll();
        circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker").reset();
    }

    private UUID createTestClaim(UUID companyOrderId, String reason, BigDecimal amount) throws Exception {
        ClaimCreateRequest createRequest = new ClaimCreateRequest(
                companyOrderId,
                "RETURN",
                reason,
                amount
        );

        String response = mockMvc.perform(post("/api/v1/claims")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).get("data").get("claimId").asText());
    }

    @Test
    @DisplayName("시나리오 1: SAGA 정상 흐름 (클레임 승인 및 재고 복원)")
    void sagaSuccessScenario() throws Exception {
        UUID companyOrderId = UUID.randomUUID();
        UUID claimId = createTestClaim(companyOrderId, "정상 환불 테스트", new BigDecimal("30000"));

        when(orderPort.getCompanyOrderDetails(any())).thenReturn(
                new OrderPort.CompanyOrderDetails(UUID.randomUUID(), companyOrderId, List.of())
        );

        ClaimStatusUpdateRequest updateRequest = new ClaimStatusUpdateRequest("PROCESSING", new BigDecimal("30000"));

        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        OrderClaim finalClaim = orderClaimRepository.findById(claimId).orElseThrow();
        assertThat(finalClaim.getStatus()).isEqualTo(ClaimStatus.PROCESSING);
    }

    @Test
    @DisplayName("시나리오 2: SAGA 보상 트랜잭션 (주문 서비스 연동 실패 시 롤백)")
    void sagaCompensationScenario() throws Exception {
        UUID companyOrderId = UUID.randomUUID();
        UUID claimId = createTestClaim(companyOrderId, "실패 테스트", new BigDecimal("50000"));

        OrderPort.CompanyOrderDetails details = new OrderPort.CompanyOrderDetails(
                UUID.randomUUID(),
                companyOrderId,
                List.of(new OrderPort.CompanyOrderDetails.ItemDetails(UUID.randomUUID(), 1))
        );
        when(orderPort.getCompanyOrderDetails(any())).thenReturn(details);
        
        doThrow(new RuntimeException("Order Service 연결 오류"))
                .when(orderPort).cancelCompanyOrderByClaim(any(), any());

        ClaimStatusUpdateRequest updateRequest = new ClaimStatusUpdateRequest("PROCESSING", new BigDecimal("50000"));

        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        verify(hubPort, atLeastOnce()).returnStock(any(), any());
        verify(hubPort, atLeastOnce()).deductStock(any(), any());
        
        OrderClaim finalClaim = orderClaimRepository.findById(claimId).orElseThrow();
        assertThat(finalClaim.getStatus()).isEqualTo(ClaimStatus.REQUESTED);
    }

    @Test
    @DisplayName("시나리오 3: 클레임 거절(REJECTED) 흐름 검증")
    void claimRejectedScenario() throws Exception {
        // [1] 클레임 생성
        UUID companyOrderId = UUID.randomUUID();
        UUID claimId = createTestClaim(companyOrderId, "거절 테스트 사유", new BigDecimal("10000"));

        // [2] 클레임 거절 처리 (MASTER 권한)
        ClaimStatusUpdateRequest rejectRequest = new ClaimStatusUpdateRequest("REJECTED", BigDecimal.ZERO);

        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rejectRequest)))
                .andExpect(status().isOk());

        // [3] 상태 검증
        OrderClaim finalClaim = orderClaimRepository.findById(claimId).orElseThrow();
        assertThat(finalClaim.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        
        // Slack 알림은 현재 SlackPort가 부재하여 상태 변경까지만 검증함
    }

    @Test
    @DisplayName("시나리오 4: 환불 금액(Refund Amount) 정합성 검증")
    void claimRefundAmountScenario() throws Exception {
        // [1] 클레임 생성 (금액 명시)
        UUID companyOrderId = UUID.randomUUID();
        BigDecimal expectedAmount = new BigDecimal("12345.67");
        UUID claimId = createTestClaim(companyOrderId, "금액 검증 테스트", expectedAmount);

        // [2] 저장된 금액 확인
        OrderClaim claim = orderClaimRepository.findById(claimId).orElseThrow();
        assertThat(claim.getRefundAmount()).isEqualByComparingTo(expectedAmount);

        // [3] 승인 시 금액 변경 업데이트 및 최종 확인
        BigDecimal updatedAmount = new BigDecimal("20000.00");
        ClaimStatusUpdateRequest updateRequest = new ClaimStatusUpdateRequest("PROCESSING", updatedAmount);
        
        when(orderPort.getCompanyOrderDetails(any())).thenReturn(
                new OrderPort.CompanyOrderDetails(UUID.randomUUID(), companyOrderId, List.of())
        );

        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        OrderClaim finalClaim = orderClaimRepository.findById(claimId).orElseThrow();
        assertThat(finalClaim.getRefundAmount()).isEqualByComparingTo(updatedAmount);
    }
}
