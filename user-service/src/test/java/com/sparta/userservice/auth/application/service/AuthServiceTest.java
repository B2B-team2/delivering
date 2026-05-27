package com.sparta.userservice.auth.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakAuthClient;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakTokenResponse;
import com.sparta.userservice.auth.presentation.dto.request.LoginRequest;
import com.sparta.userservice.auth.presentation.dto.request.SignupRequest;
import com.sparta.userservice.auth.presentation.dto.response.LoginResponse;
import com.sparta.userservice.delivery.infrastructure.repository.DeliveryManagerRepository;
import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import com.sparta.userservice.user.domain.enums.Role;
import com.sparta.userservice.user.infrastructure.repository.CompanyManagerRepository;
import com.sparta.userservice.user.infrastructure.repository.HubManagerRepository;
import com.sparta.userservice.user.infrastructure.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HubManagerRepository hubManagerRepository;

    @Mock
    private DeliveryManagerRepository deliveryManagerRepository;

    @Mock
    private CompanyManagerRepository companyManagerRepository;

    @Mock
    private KeycloakAuthClient keycloakAuthClient;

    @Mock
    private TokenService tokenService;

    // ===== signup =====

    @Test
    @DisplayName("회원가입 - COMPANY_MANAGER 성공")
    void signup_companyManager_success() {
        SignupRequest request = createSignupRequest("test@test.com", "Password1!", "테스트", "010-1234-5678", "U123", Role.COMPANY_MANAGER);

        given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(false);
        given(keycloakAuthClient.existsUser(anyString())).willReturn(false);

        authService.signup(request);

        verify(userRepository).save(any());
        verify(companyManagerRepository).save(any());
        verify(keycloakAuthClient).createUser(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("회원가입 - HUB_MANAGER 성공")
    void signup_hubManager_success() {
        SignupRequest request = createSignupRequest("test@test.com", "Password1!", "테스트", "010-1234-5678", "U123", Role.HUB_MANAGER);

        given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(false);
        given(keycloakAuthClient.existsUser(anyString())).willReturn(false);

        authService.signup(request);

        verify(hubManagerRepository).save(any());
    }

    @Test
    @DisplayName("회원가입 - MASTER 역할이면 예외")
    void signup_masterRole_exception() {
        SignupRequest request = createSignupRequest("test@test.com", "Password1!", "테스트", "010-1234-5678", "U123", Role.MASTER);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복이면 예외")
    void signup_duplicateEmail_exception() {
        SignupRequest request = createSignupRequest("test@test.com", "Password1!", "테스트", "010-1234-5678", "U123", Role.COMPANY_MANAGER);

        given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(true);

        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("회원가입 - Keycloak 잔존 유저 삭제 후 재가입")
    void signup_keycloakUserExists_deleteAndRecreate() {
        SignupRequest request = createSignupRequest("test@test.com", "Password1!", "테스트", "010-1234-5678", "U123", Role.COMPANY_MANAGER);

        given(userRepository.existsByEmailAndDeletedAtIsNull(anyString())).willReturn(false);
        given(keycloakAuthClient.existsUser(anyString())).willReturn(true);

        authService.signup(request);

        verify(keycloakAuthClient).deleteUser(anyString());
        verify(keycloakAuthClient).createUser(anyString(), anyString(), anyString());
    }

    // ===== login =====

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        LoginRequest request = createLoginRequest("test@test.com", "Password1!");
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);
        KeycloakTokenResponse tokenResponse = createKeycloakTokenResponse("accessToken", "refreshToken");

        given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(user));
        given(keycloakAuthClient.login(anyString(), anyString())).willReturn(tokenResponse);

        LoginResponse result = authService.login(request);

        assertThat(result.getAccessToken()).isEqualTo("accessToken");
        verify(tokenService).saveRefreshToken(any(), anyString(), anyLong());
    }

    @Test
    @DisplayName("로그인 - 유저 없으면 예외")
    void login_userNotFound() {
        LoginRequest request = createLoginRequest("test@test.com", "Password1!");

        given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("로그인 - 미승인 유저면 예외")
    void login_notApproved() {
        LoginRequest request = createLoginRequest("test@test.com", "Password1!");
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.PENDING);

        given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class);
    }

    // ===== logout =====

    @Test
    @DisplayName("로그아웃 - 성공")
    void logout_success() {
        String accessToken = "eyJhbGciOiJSUzI1NiJ9.eyJleHAiOjk5OTk5OTk5OTl9.signature";
        String refreshToken = "refreshToken";
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);

        given(tokenService.isBlacklisted(accessToken)).willReturn(false);
        given(keycloakAuthClient.extractEmail(refreshToken)).willReturn("test@test.com");
        given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(user));

        authService.logout(accessToken, refreshToken);

        verify(tokenService).blacklistAccessToken(anyString(), anyLong());
        verify(tokenService).deleteRefreshToken(any());
        verify(keycloakAuthClient).logout(refreshToken);
    }

    @Test
    @DisplayName("로그아웃 - 이미 로그아웃된 토큰이면 예외")
    void logout_alreadyBlacklisted() {
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        given(tokenService.isBlacklisted(accessToken)).willReturn(true);

        assertThatThrownBy(() -> authService.logout(accessToken, refreshToken))
                .isInstanceOf(BusinessException.class);
    }

    // ===== refresh =====

    @Test
    @DisplayName("토큰 재발급 - 성공")
    void refresh_success() {
        String refreshToken = "refreshToken";
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);
        KeycloakTokenResponse tokenResponse = createKeycloakTokenResponse("newAccessToken", "newRefreshToken");

        given(keycloakAuthClient.extractEmail(refreshToken)).willReturn("test@test.com");
        given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(user));
        given(tokenService.getRefreshToken(any())).willReturn(refreshToken);
        given(keycloakAuthClient.refresh(refreshToken)).willReturn(tokenResponse);

        LoginResponse result = authService.refresh(refreshToken);

        assertThat(result.getAccessToken()).isEqualTo("newAccessToken");
        verify(tokenService).saveRefreshToken(any(), anyString(), anyLong());
    }

    @Test
    @DisplayName("토큰 재발급 - 저장된 토큰과 다르면 예외")
    void refresh_invalidToken() {
        String refreshToken = "refreshToken";
        User user = createUser(Role.COMPANY_MANAGER, ApprovalStatus.APPROVED);

        given(keycloakAuthClient.extractEmail(refreshToken)).willReturn("test@test.com");
        given(userRepository.findByEmailAndDeletedAtIsNull(anyString())).willReturn(Optional.of(user));
        given(tokenService.getRefreshToken(any())).willReturn("differentToken");

        assertThatThrownBy(() -> authService.refresh(refreshToken))
                .isInstanceOf(BusinessException.class);
    }

    // ===== 헬퍼 =====

    private User createUser(Role role, ApprovalStatus status) {
        User user = User.create("test@test.com", "KEYCLOAK_MANAGED", "테스트", "010-1234-5678", "U123", role);
        if (status == ApprovalStatus.APPROVED) {
            user.approve(UUID.randomUUID());
        }
        return user;
    }

    private SignupRequest createSignupRequest(String email, String password, String name, String phone, String slackId, Role role) {
        SignupRequest request = new SignupRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        ReflectionTestUtils.setField(request, "name", name);
        ReflectionTestUtils.setField(request, "phone", phone);
        ReflectionTestUtils.setField(request, "slackId", slackId);
        ReflectionTestUtils.setField(request, "role", role);
        return request;
    }

    private LoginRequest createLoginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        ReflectionTestUtils.setField(request, "email", email);
        ReflectionTestUtils.setField(request, "password", password);
        return request;
    }

    private KeycloakTokenResponse createKeycloakTokenResponse(String accessToken, String refreshToken) {
        return new KeycloakTokenResponse(accessToken, refreshToken, "Bearer", 3600L, 7200L);
    }
}