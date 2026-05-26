package com.sparta.userservice.admin.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.PageResponse;
import com.sparta.userservice.admin.infrastructure.repository.AdminRepository;
import com.sparta.userservice.admin.presentation.dto.request.ApprovalRequest;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakAuthClient;
import com.sparta.userservice.global.config.security.util.SecurityUtils;
import com.sparta.userservice.user.domain.entity.Admin;
import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import com.sparta.userservice.user.domain.enums.Role;
import com.sparta.userservice.user.infrastructure.repository.CompanyManagerRepository;
import com.sparta.userservice.user.infrastructure.repository.UserRepository;
import com.sparta.userservice.user.presentation.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private KeycloakAuthClient keycloakAuthClient;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private CompanyManagerRepository companyManagerRepository;

    // ===== getAllUsers =====

    @Test
    @DisplayName("전체 사용자 조회 - 마스터 관리자 성공")
    void getAllUsers_success() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);
        Page<User> page = new PageImpl<>(List.of(user));

        given(securityUtils.isNotMaster()).willReturn(false);
        given(userRepository.findAllByDeletedAtIsNull(any())).willReturn(page);

        PageResponse<UserResponse> result = adminService.getAllUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("전체 사용자 조회 - 마스터가 아니면 예외")
    void getAllUsers_forbidden() {
        Pageable pageable = PageRequest.of(0, 10);
        given(securityUtils.isNotMaster()).willReturn(true);

        assertThatThrownBy(() -> adminService.getAllUsers(pageable))
                .isInstanceOf(BusinessException.class);
    }

    // ===== getPendingUsers =====

    @Test
    @DisplayName("가입 대기 목록 조회 - 마스터 성공")
    void getPendingUsers_master_success() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.PENDING);
        Page<User> page = new PageImpl<>(List.of(user));

        given(securityUtils.isNotMaster()).willReturn(false);
        given(userRepository.findAllByApprovalStatusAndDeletedAtIsNull(any(), any())).willReturn(page);

        PageResponse<UserResponse> result = adminService.getPendingUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("가입 대기 목록 조회 - 허브 관리자 성공")
    void getPendingUsers_hubManager_success() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.PENDING);
        Page<User> page = new PageImpl<>(List.of(user));

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(false);
        given(userRepository.findAllByApprovalStatusAndDeletedAtIsNull(any(), any())).willReturn(page);

        PageResponse<UserResponse> result = adminService.getPendingUsers(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("가입 대기 목록 조회 - 권한 없으면 예외")
    void getPendingUsers_forbidden() {
        Pageable pageable = PageRequest.of(0, 10);
        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(true);

        assertThatThrownBy(() -> adminService.getPendingUsers(pageable))
                .isInstanceOf(BusinessException.class);
    }

    // ===== processApproval =====

    @Test
    @DisplayName("가입 승인 - 성공")
    void processApproval_approve_success() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.PENDING);
        Admin admin = Admin.create(createUser(Role.MASTER, ApprovalStatus.APPROVED));
        ApprovalRequest request = createApprovalRequest(ApprovalStatus.APPROVED, null);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));
        given(companyManagerRepository.findById(userId)).willReturn(Optional.empty());

        adminService.processApproval(userId, adminId, request);

        verify(keycloakAuthClient).enableUser(user.getEmail());
        assertThat(user.getApprovalStatus()).isEqualTo(ApprovalStatus.APPROVED);
    }

    @Test
    @DisplayName("가입 거절 - 성공")
    void processApproval_reject_success() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.PENDING);
        Admin admin = Admin.create(createUser(Role.MASTER, ApprovalStatus.APPROVED));
        ApprovalRequest request = createApprovalRequest(ApprovalStatus.REJECTED, "부적절한 요청");

        given(securityUtils.isNotMaster()).willReturn(false);
        given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        adminService.processApproval(userId, adminId, request);

        assertThat(user.getApprovalStatus()).isEqualTo(ApprovalStatus.REJECTED);
        assertThat(user.getRejectedReason()).isEqualTo("부적절한 요청");
    }

    @Test
    @DisplayName("가입 승인 - 권한 없으면 예외")
    void processApproval_forbidden() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        ApprovalRequest request = createApprovalRequest(ApprovalStatus.APPROVED, null);

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.isNotHubManager()).willReturn(true);

        assertThatThrownBy(() -> adminService.processApproval(userId, adminId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("가입 승인 - PENDING이 아닌 상태면 예외")
    void processApproval_invalidStatus() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);
        Admin admin = Admin.create(createUser(Role.MASTER, ApprovalStatus.APPROVED));
        ApprovalRequest request = createApprovalRequest(ApprovalStatus.APPROVED, null);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> adminService.processApproval(userId, adminId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("가입 승인 - 유저 없으면 예외")
    void processApproval_userNotFound() {
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();
        Admin admin = Admin.create(createUser(Role.MASTER, ApprovalStatus.APPROVED));
        ApprovalRequest request = createApprovalRequest(ApprovalStatus.APPROVED, null);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(adminRepository.findById(adminId)).willReturn(Optional.of(admin));
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.processApproval(userId, adminId, request))
                .isInstanceOf(BusinessException.class);
    }

    // ===== 헬퍼 =====

    private User createUser(Role role, ApprovalStatus status) {
        User user = User.create("test@test.com", "KEYCLOAK_MANAGED", "테스트", "010-1234-5678", "U123", role);
        if (status == ApprovalStatus.APPROVED) {
            user.approve(UUID.randomUUID());
        } else if (status == ApprovalStatus.REJECTED) {
            user.reject(UUID.randomUUID(), "거절 사유");
        }
        return user;
    }

    private ApprovalRequest createApprovalRequest(ApprovalStatus status, String rejectedReason) {
        ApprovalRequest request = new ApprovalRequest();
        ReflectionTestUtils.setField(request, "status", status);
        ReflectionTestUtils.setField(request, "rejectedReason", rejectedReason);
        return request;
    }
}