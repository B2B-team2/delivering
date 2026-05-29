package com.sparta.operationsservice.claim.presentation.controller;

import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import com.sparta.operationsservice.claim.application.service.ClaimService;
import com.sparta.operationsservice.global.application.service.AuthService;
import com.sparta.operationsservice.global.config.SecurityConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
@Import(SecurityConfig.class)
class ClaimControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService claimService;

    @MockBean(name = "authService")
    private AuthService authService;

    @MockBean
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @MockBean
    private TimeLimiterRegistry timeLimiterRegistry;

    @Test
    @DisplayName("클레임 목록 조회 - MASTER 권한 성공")
    void getClaims_Master_Success() throws Exception {
        when(claimService.getClaims(any())).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/claims")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("클레임 목록 조회 - 업체 담당자 실패 (403)")
    void getClaims_CompanyManager_Forbidden() throws Exception {
        mockMvc.perform(get("/api/v1/claims")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "COMPANY_MANAGER"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("클레임 접수 - 업체 담당자 성공")
    void createClaim_CompanyManager_Success() throws Exception {
        when(claimService.createClaim(any())).thenReturn(ClaimDto.builder().claimId(UUID.randomUUID()).build());

        mockMvc.perform(post("/api/v1/claims")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "COMPANY_MANAGER")
                        .contentType("application/json")
                        .content("{\"companyOrderId\":\"" + UUID.randomUUID() + "\", \"claimType\":\"RETURN\", \"reason\":\"Defective\"}"))
                .andExpect(status().isCreated());
    }
}
