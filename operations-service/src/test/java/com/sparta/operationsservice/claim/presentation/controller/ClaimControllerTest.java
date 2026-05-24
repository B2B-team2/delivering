package com.sparta.operationsservice.claim.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.handler.GlobalExceptionHandler;
import com.sparta.operationsservice.claim.application.dto.ClaimCreateCommand;
import com.sparta.operationsservice.claim.application.dto.ClaimDto;
import com.sparta.operationsservice.claim.application.dto.ClaimStatusUpdateCommand;
import com.sparta.operationsservice.claim.application.service.ClaimService;
import com.sparta.operationsservice.claim.presentation.dto.ClaimCreateRequest;
import com.sparta.operationsservice.claim.presentation.dto.ClaimStatusUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimController.class)
@Import(GlobalExceptionHandler.class)
class ClaimControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClaimService claimService;

    @Test
    @WithMockUser
    @DisplayName("API 응답 규격 검증: POST /api/v1/claims 호출 시 201 Created와 ApiResponse 포맷이 유지되는가?")
    void createClaimApiResponseFormatTest() throws Exception {
        // given
        ClaimCreateRequest request = ClaimCreateRequest.builder()
                .orderItemId(UUID.randomUUID())
                .claimType("RETURN")
                .reason("Test reason")
                .refundAmount(BigDecimal.valueOf(10000))
                .build();

        ClaimDto responseDto = ClaimDto.builder()
                .claimId(UUID.randomUUID())
                .reason(request.getReason())
                .build();

        when(claimService.createClaim(any(ClaimCreateCommand.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/v1/claims")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.data.reason").value(request.getReason()));
    }

    @Test
    @WithMockUser
    @DisplayName("API 응답 규격 검증: GET /api/v1/claims 호출 시 200 OK와 ApiResponse 포맷이 유지되는가?")
    void getClaimsApiResponseFormatTest() throws Exception {
        // given
        ClaimDto dto = ClaimDto.builder().claimId(UUID.randomUUID()).build();
        when(claimService.getClaims(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(dto)));

        // when & then
        mockMvc.perform(get("/api/v1/claims")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("API 응답 규격 검증: GET /api/v1/claims/{claimId} 호출 시 200 OK 확인")
    void getClaimApiResponseFormatTest() throws Exception {
        // given
        UUID claimId = UUID.randomUUID();
        ClaimDto responseDto = ClaimDto.builder().claimId(claimId).build();

        when(claimService.getClaim(claimId)).thenReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/claims/{claimId}", claimId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.claimId").value(claimId.toString()));
    }

    @Test
    @WithMockUser
    @DisplayName("API 응답 규격 검증: PATCH /api/v1/claims/{claimId}/status 호출 시 200 OK 확인")
    void updateClaimStatusApiResponseFormatTest() throws Exception {
        // given
        UUID claimId = UUID.randomUUID();
        ClaimStatusUpdateRequest request = ClaimStatusUpdateRequest.builder()
                .status("COMPLETED")
                .build();

        ClaimDto responseDto = ClaimDto.builder()
                .claimId(claimId)
                .status("COMPLETED")
                .build();

        when(claimService.updateClaimStatus(eq(claimId), any(ClaimStatusUpdateCommand.class))).thenReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/api/v1/claims/{claimId}/status", claimId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }
}
