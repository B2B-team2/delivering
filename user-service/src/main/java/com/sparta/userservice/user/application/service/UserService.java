package com.sparta.userservice.user.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakAuthClient;
import com.sparta.userservice.global.config.security.util.SecurityUtils;
import com.sparta.userservice.global.exception.UserErrorCode;
import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.infrastructure.repository.UserRepository;
import com.sparta.userservice.user.presentation.dto.request.UserUpdateRequest;
import com.sparta.userservice.user.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakAuthClient keycloakAuthClient;
    private final SecurityUtils securityUtils;

    public UserResponse getUser(UUID userId) {
        // MASTER는 모두 조회 가능, 나머지는 본인만
        if (securityUtils.isNotMaster() &&
                !securityUtils.getUserId().equals(userId.toString())) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        return new UserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(UUID userId, UserUpdateRequest request) {
        // MASTER는 모두 수정 가능, 나머지는 본인만
        if (securityUtils.isNotMaster() &&
                !securityUtils.getUserId().equals(userId.toString())) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        user.updateProfile(request.getName(), request.getPhone(), request.getSlackId());
        return new UserResponse(user);
    }

    @Transactional
    public void deleteUser(UUID userId, String deletedBy) {
        // MASTER만 삭제 가능
        if (securityUtils.isNotMaster()) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.isDeleted()) {
            throw new BusinessException(UserErrorCode.USER_ALREADY_DELETED);
        }

        keycloakAuthClient.disableUser(user.getEmail());
        user.softDelete(deletedBy);
    }
}