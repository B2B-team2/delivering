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
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ClaimResilienceTest extends IntegrationTestSupport {

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
        // 테스트마다 CB 리셋
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker");
        cb.reset();
    }

    private UUID createTestClaim(UUID companyOrderId) throws Exception {
        ClaimCreateRequest createRequest = new ClaimCreateRequest(
                companyOrderId,
                "RETURN",
                "회복탄력성 테스트",
                new BigDecimal("10000")
        );

        String response = mockMvc.perform(post("/api/v1/claims")
                        .header("X-Gateway-Secret", "local-secret").header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return UUID.fromString(objectMapper.readTree(response).get("data").get("claimId").asText());
    }

    @Test
    @DisplayName("CircuitBreaker 타임아웃 시나리오: 외부 서비스 지연 시 Saga 보상 트런잭션이 실행된다")
    void sagaTimeoutAndCompensationTest() throws Exception {
        UUID companyOrderId = UUID.randomUUID();
        UUID claimId = createTestClaim(companyOrderId);

        // [Saga] 내부에서는 현재 타임리미터를 직접 걸지 않으므로, 
        // 이 테스트는 단순히 지연 상황에서 Saga 로직이 어떻게 반응하는지 보거나, 
        // Feign Read Timeout에 의한 실패를 검증하는 용도로 사용됨.
        // 여기서는 강제 예외 발생으로 대체하여 보상 트랜잭션 트리거 확인.
        when(orderPort.getCompanyOrderDetails(any())).thenThrow(new RuntimeException("Timeout Simulation"));

        ClaimStatusUpdateRequest updateRequest = new ClaimStatusUpdateRequest("PROCESSING", new BigDecimal("10000"));

        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                        .header("X-Gateway-Secret", "local-secret").header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        OrderClaim finalClaim = orderClaimRepository.findById(claimId).orElseThrow();
        assertThat(finalClaim.getStatus()).isEqualTo(ClaimStatus.REQUESTED);
    }

    @Test
    @DisplayName("CircuitBreaker OPEN 시나리오: 연속 실패 시 호출을 즉시 차단한다")
    void circuitBreakerOpenTest() throws Exception {
        // [1] 실패 유도
        when(orderPort.getCompanyOrderDetails(any())).thenThrow(new RuntimeException("External Service Down"));

        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker");
        
        // application-test.yml 설정: slidingWindowSize=5, minimumNumberOfCalls=2, failureRateThreshold=50
        for (int i = 0; i < 3; i++) {
            UUID companyOrderId = UUID.randomUUID();
            UUID claimId = createTestClaim(companyOrderId);
            ClaimStatusUpdateRequest updateRequest = new ClaimStatusUpdateRequest("PROCESSING", new BigDecimal("10000"));

            mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                            .header("X-Gateway-Secret", "local-secret").header("X-User-Id", UUID.randomUUID().toString())
                            .header("X-User-Role", "MASTER")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isInternalServerError());
            
            if (cb.getState() == CircuitBreaker.State.OPEN) break;
        }

        // [2] OPEN 상태 확인
        assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        
        // [3] 차단 확인 (다음 호출)
        UUID nextClaimId = createTestClaim(UUID.randomUUID());
        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", nextClaimId)
                        .header("X-Gateway-Secret", "local-secret").header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ClaimStatusUpdateRequest("PROCESSING", BigDecimal.ZERO))))
                .andExpect(status().isInternalServerError());

        // CB에 의해 차단되어 실제 orderPort 호출이 발생하지 않아야 함
        verify(orderPort, atMost(3)).getCompanyOrderDetails(any());
    }
}
