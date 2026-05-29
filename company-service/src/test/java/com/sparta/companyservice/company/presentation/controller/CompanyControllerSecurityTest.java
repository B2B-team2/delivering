package com.sparta.companyservice.company.presentation.controller;

import com.sparta.companyservice.company.application.dto.CompanyDto;
import com.sparta.companyservice.company.application.service.CompanyAddressService;
import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.global.application.service.AuthService;
import com.sparta.companyservice.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@Import(SecurityConfig.class)
class CompanyControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private CompanyAddressService companyAddressService;

    @MockBean(name = "authService")
    private AuthService authService;

    @Test
    @DisplayName("업체 등록 - MASTER 권한 성공")
    void createCompany_Master_Success() throws Exception {
        when(companyService.createCompany(any())).thenReturn(CompanyDto.builder().companyId(UUID.randomUUID()).build());

        mockMvc.perform(post("/api/v1/companies")
                        .header("X-Gateway-Secret", "local-secret")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "MASTER")
                        .contentType("application/json")
                        .content("{\"companyName\":\"Test\", \"companyType\":\"PRODUCER\", \"businessNumber\":\"123\", \"hubId\":\"" + UUID.randomUUID() + "\", \"latitude\":37.5, \"longitude\":127.0, \"address\":\"Seoul\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("업체 등록 - 일반 사용자 실패 (403)")
    void createCompany_User_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/companies")
                        .header("X-Gateway-Secret", "local-secret")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "USER")
                        .contentType("application/json")
                        .content("{\"companyName\":\"Test\", \"companyType\":\"PRODUCER\", \"businessNumber\":\"123\", \"hubId\":\"" + UUID.randomUUID() + "\", \"latitude\":37.5, \"longitude\":127.0, \"address\":\"Seoul\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("업체 삭제 - HUB_MANAGER 권한 성공")
    void deleteCompany_HubManager_Success() throws Exception {
        UUID companyId = UUID.randomUUID();
        when(companyService.deleteCompany(any(), any())).thenReturn(CompanyDto.builder().companyId(companyId).build());

        mockMvc.perform(delete("/api/v1/companies/" + companyId)
                        .header("X-Gateway-Secret", "local-secret")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .header("X-User-Role", "HUB_MANAGER"))
                .andExpect(status().isOk());
    }
}