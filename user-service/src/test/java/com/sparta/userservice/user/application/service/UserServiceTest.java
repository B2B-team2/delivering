package com.sparta.userservice.user.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakAuthClient;
import com.sparta.userservice.global.config.security.util.SecurityUtils;
import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import com.sparta.userservice.user.domain.enums.Role;
import com.sparta.userservice.user.infrastructure.repository.UserRepository;
import com.sparta.userservice.user.presentation.dto.request.UserUpdateRequest;
import com.sparta.userservice.user.presentation.dto.response.UserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakAuthClient keycloakAuthClient;

    @Mock
    private SecurityUtils securityUtils;

    // ===== getUser =====

    @Test
    @DisplayName("유저 조회 - 마스터 성공")
    void getUser_master_success() {
        UUID userId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        UserResponse result = userService.getUser(userId);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("유저 조회 - 본인 성공")
    void getUser_self_success() {
        UUID userId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.getUserId()).willReturn(userId.toString());
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        UserResponse result = userService.getUser(userId);

        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("유저 조회 - 타인 조회 시 예외")
    void getUser_otherUser_forbidden() {
        UUID userId = UUID.randomUUID();

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.getUserId()).willReturn(UUID.randomUUID().toString());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("유저 조회 - 유저 없으면 예외")
    void getUser_notFound() {
        UUID userId = UUID.randomUUID();

        given(securityUtils.isNotMaster()).willReturn(false);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(BusinessException.class);
    }

    // ===== updateUser =====

    @Test
    @DisplayName("유저 수정 - 본인 성공")
    void updateUser_self_success() {
        UUID userId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);
        UserUpdateRequest request = createUpdateRequest("새이름", "010-9999-9999", "U999");

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.getUserId()).willReturn(userId.toString());
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        UserResponse result = userService.updateUser(userId, request);

        assertThat(result.getName()).isEqualTo("새이름");
    }

    @Test
    @DisplayName("유저 수정 - 타인 수정 시 예외")
    void updateUser_otherUser_forbidden() {
        UUID userId = UUID.randomUUID();
        UserUpdateRequest request = createUpdateRequest("새이름", "010-9999-9999", "U999");

        given(securityUtils.isNotMaster()).willReturn(true);
        given(securityUtils.getUserId()).willReturn(UUID.randomUUID().toString());

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessException.class);
    }

    // ===== deleteUser =====

    @Test
    @DisplayName("유저 삭제 - 마스터 성공")
    void deleteUser_master_success() {
        UUID userId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);

        given(securityUtils.isNotMaster()).willReturn(false);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        userService.deleteUser(userId, "master@test.com");

        verify(keycloakAuthClient).disableUser(user.getEmail());
    }

    @Test
    @DisplayName("유저 삭제 - 마스터가 아니면 예외")
    void deleteUser_forbidden() {
        UUID userId = UUID.randomUUID();

        given(securityUtils.isNotMaster()).willReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(userId, "other@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("유저 삭제 - 이미 삭제된 유저면 예외")
    void deleteUser_alreadyDeleted() {
        UUID userId = UUID.randomUUID();
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);
        user.softDelete("master@test.com");

        given(securityUtils.isNotMaster()).willReturn(false);
        given(userRepository.findByIdAndDeletedAtIsNull(userId)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.deleteUser(userId, "master@test.com"))
                .isInstanceOf(BusinessException.class);
    }

    // ===== 헬퍼 =====

    private User createUser(Role role, ApprovalStatus status) {
        User user = User.create(UUID.randomUUID(), "test@test.com", "KEYCLOAK_MANAGED", "테스트", "010-1234-5678", "U123", role);
        if (status == ApprovalStatus.APPROVED) {
            user.approve(UUID.randomUUID());
        }
        return user;
    }

    private UserUpdateRequest createUpdateRequest(String name, String phone, String slackId) {
        UserUpdateRequest request = new UserUpdateRequest();
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "phone", phone);
        ReflectionTestUtils.setField(request, "slackId", slackId);
        return request;
    }
}