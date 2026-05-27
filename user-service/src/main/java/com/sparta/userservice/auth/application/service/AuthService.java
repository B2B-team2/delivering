package com.sparta.userservice.auth.application.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sparta.common.dto.BusinessException;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakAuthClient;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakTokenResponse;
import com.sparta.userservice.user.infrastructure.repository.CompanyManagerRepository;
import com.sparta.userservice.delivery.infrastructure.repository.DeliveryManagerRepository;
import com.sparta.userservice.user.infrastructure.repository.HubManagerRepository;
import com.sparta.userservice.auth.presentation.dto.request.LoginRequest;
import com.sparta.userservice.auth.presentation.dto.request.SignupRequest;
import com.sparta.userservice.auth.presentation.dto.response.LoginResponse;
import com.sparta.userservice.global.exception.AuthErrorCode;
import com.sparta.userservice.global.exception.UserErrorCode;
import com.sparta.userservice.user.domain.entity.CompanyManager;
import com.sparta.userservice.user.domain.entity.DeliveryManager;
import com.sparta.userservice.user.domain.entity.HubManager;
import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import com.sparta.userservice.user.domain.enums.ManagerType;
import com.sparta.userservice.user.domain.enums.Role;
import com.sparta.userservice.user.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final HubManagerRepository hubManagerRepository;
    private final DeliveryManagerRepository deliveryManagerRepository;
    private final CompanyManagerRepository companyManagerRepository;
    private final KeycloakAuthClient keycloakAuthClient;
    private final TokenService tokenService;

    @Transactional
    public void signup(SignupRequest request) {

        Role role = request.getRole() != null ? request.getRole() : Role.COMPANY_MANAGER;

        if (role == Role.MASTER) {
            throw new BusinessException(UserErrorCode.CANNOT_REGISTER_AS_MASTER);
        }

        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new BusinessException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // Keycloak 잔존하는 유저 정리
        if (keycloakAuthClient.existsUser(request.getEmail())) {
            try {
                keycloakAuthClient.deleteUser(request.getEmail());
                log.warn("[AUTH] Keycloak 잔존 유저 삭제 후 재가입 진행 - email={}", request.getEmail());
            } catch (IllegalStateException e) {
                throw new BusinessException(AuthErrorCode.KEYCLOAK_USER_DELETE_FAILED);
            }
        }

        // Keycloak 먼저 생성 → ID 받아서 DB에 저장
        UUID keycloakId;
        try {
            keycloakId = keycloakAuthClient.createUser(request.getEmail(), request.getPassword(), role.name());
        } catch (IllegalStateException e) {
            throw new BusinessException(AuthErrorCode.KEYCLOAK_USER_CREATE_FAILED);
        }

        User user = User.create(
                keycloakId,
                request.getEmail(),
                "KEYCLOAK_MANAGED",
                request.getName(),
                request.getPhone(),
                request.getSlackId(),
                role
        );
        userRepository.save(user);

        switch (role) {
            case HUB_MANAGER -> hubManagerRepository.save(HubManager.create(user));
            case HUB_DELIVERY_MANAGER -> {
                int nextOrder = deliveryManagerRepository.countByDeletedAtIsNull();
                deliveryManagerRepository.save(
                        DeliveryManager.create(user, ManagerType.HUB_DELIVERY, nextOrder)
                );
            }
            case COMPANY_DELIVERY_MANAGER -> {
                int nextOrder = deliveryManagerRepository.countByDeletedAtIsNull();
                deliveryManagerRepository.save(
                        DeliveryManager.create(user, ManagerType.COMPANY_DELIVERY, nextOrder)
                );
            }
            case COMPANY_MANAGER -> companyManagerRepository.save(
                    CompanyManager.create(user)
            );
            default -> throw new BusinessException(UserErrorCode.INVALID_ROLE);
        }
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new BusinessException(UserErrorCode.USER_NOT_APPROVED);
        }

        KeycloakTokenResponse tokenResponse = keycloakAuthClient.login(
                request.getEmail(),
                request.getPassword()
        );

        tokenService.saveRefreshToken(user.getId(), tokenResponse.getRefreshToken(), tokenResponse.getRefreshExpiresIn());

        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    public LoginResponse refresh(String refreshToken) {

        String email = keycloakAuthClient.extractEmail(refreshToken);

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        String storedRefreshToken = tokenService.getRefreshToken(user.getId());

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(UserErrorCode.INVALID_REFRESH_TOKEN);
        }

        KeycloakTokenResponse tokenResponse = keycloakAuthClient.refresh(refreshToken);

        tokenService.saveRefreshToken(user.getId(), tokenResponse.getRefreshToken(), tokenResponse.getRefreshExpiresIn());

        return new LoginResponse(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
    }

    public void logout(String accessToken, String refreshToken) {

        // 이미 로그아웃된 토큰 체크
        if (tokenService.isBlacklisted(accessToken)) {
            throw new BusinessException(UserErrorCode.INVALID_REFRESH_TOKEN);
        }

        String email = keycloakAuthClient.extractEmail(refreshToken);

        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        DecodedJWT decoded = JWT.decode(accessToken);
        long remainingSeconds = decoded.getExpiresAt().toInstant().getEpochSecond()
                - Instant.now().getEpochSecond();

        tokenService.blacklistAccessToken(accessToken, Math.max(remainingSeconds, 0));
        tokenService.deleteRefreshToken(user.getId());
        keycloakAuthClient.logout(refreshToken);
    }
}