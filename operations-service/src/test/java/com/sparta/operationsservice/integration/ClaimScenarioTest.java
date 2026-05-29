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

    @MockBean
    private OrderPort orderPort;

    @MockBean
    private HubPort hubPort;

    @BeforeEach
    void setUp() {
        orderClaimJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("SAGA 보상 트랜잭션 시나리오: 주문 상태 변경 실패 시 이미 수행된 재고 복원을 취소(재차 감)한다")
    void sagaCompensationScenario() throws Exception {
        // [1] 사전 준비: 클레임 생성
        UUID companyOrderId = UUID.randomUUID();
        ClaimCreateRequest createRequest = new ClaimCreateRequest(
                companyOrderId,
                "RETURN",
                "테스트 사유",
                new BigDecimal("50000")
        );

        mockMvc.perform(post("/api/v1/claims")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        OrderClaim claim = orderClaimRepository.findAll(Pageable.unpaged()).getContent().stream()
                .filter(c -> c.getCompanyOrderId().equals(companyOrderId))
                .findFirst().orElseThrow();
        UUID claimId = claim.getClaimId();

        // [2] 외부 서비스 시뮬레이션 설정
        OrderPort.CompanyOrderDetails details = new OrderPort.CompanyOrderDetails(
                UUID.randomUUID(),
                companyOrderId,
                List.of(new OrderPort.CompanyOrderDetails.ItemDetails(UUID.randomUUID(), 1))
        );
        when(orderPort.getCompanyOrderDetails(any())).thenReturn(details);
        
        doThrow(new RuntimeException("Order Service 연결 오류"))
                .when(orderPort).cancelCompanyOrderByClaim(any(), any());

        // [3] 클레임 승인 요청 (SAGA 시작)
        ClaimStatusUpdateRequest updateRequest = new ClaimStatusUpdateRequest("PROCESSING", new BigDecimal("50000"));

        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isInternalServerError());

        // [4] 검증
        verify(hubPort, atLeastOnce()).returnStock(any(), any());
        verify(hubPort, atLeastOnce()).deductStock(any(), any());
        
        OrderClaim finalClaim = orderClaimRepository.findById(claimId).orElseThrow();
        assertThat(finalClaim.getStatus()).isEqualTo(ClaimStatus.REQUESTED);
    }

    @Test
    @DisplayName("SAGA 정상 흐름 시나리오: 모든 외부 서비스 호출 성공 후 클레임 상태가 PROCESSING으로 변경된다")
    void sagaSuccessScenario() throws Exception {
        UUID companyOrderId = UUID.randomUUID();
        ClaimCreateRequest createRequest = new ClaimCreateRequest(
                companyOrderId,
                "RETURN",
                "정상 환불 테스트",
                new BigDecimal("30000")
        );

        mockMvc.perform(post("/api/v1/claims")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        OrderClaim claim = orderClaimRepository.findAll(Pageable.unpaged()).getContent().stream()
                .filter(c -> c.getCompanyOrderId().equals(companyOrderId))
                .findFirst().orElseThrow();
        UUID claimId = claim.getClaimId();

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
}
