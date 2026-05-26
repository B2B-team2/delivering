package com.sparta.userservice.admin.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.common.dto.PageResponse;
import com.sparta.userservice.admin.infrastructure.repository.AdminRepository;
import com.sparta.userservice.admin.presentation.dto.request.ApprovalRequest;
import com.sparta.userservice.auth.infrastructure.keycloak.KeycloakAuthClient;
import com.sparta.userservice.global.config.security.util.SecurityUtils;
import com.sparta.userservice.global.exception.UserErrorCode;
import com.sparta.userservice.user.domain.entity.User;
import com.sparta.userservice.user.domain.enums.ApprovalStatus;
import com.sparta.userservice.user.infrastructure.repository.UserRepository;
import com.sparta.userservice.user.presentation.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sparta.userservice.user.domain.enums.Role;
import com.sparta.userservice.user.infrastructure.repository.CompanyManagerRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final KeycloakAuthClient keycloakAuthClient;
    private final SecurityUtils securityUtils;
    private final CompanyManagerRepository companyManagerRepository;

    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        if (securityUtils.isNotMaster()) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageResponse<>(userRepository.findAllByDeletedAtIsNull(sorted)
                .map(UserResponse::new));
    }

    public PageResponse<UserResponse> getPendingUsers(Pageable pageable) {
        if (securityUtils.isNotMaster() && securityUtils.isNotHubManager()) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }
        Pageable sorted = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return new PageResponse<>(userRepository.findAllByApprovalStatusAndDeletedAtIsNull(
                ApprovalStatus.PENDING, sorted).map(UserResponse::new));
    }

    @Transactional
    public void processApproval(UUID userId, UUID adminId, ApprovalRequest request) {
        if (securityUtils.isNotMaster() && securityUtils.isNotHubManager()) {
            throw new BusinessException(UserErrorCode.FORBIDDEN);
        }

        adminRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));

        if (user.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new BusinessException(UserErrorCode.INVALID_APPROVAL_STATUS);
        }

        if (request.getStatus() == ApprovalStatus.APPROVED) {
            user.approve(adminId);
            keycloakAuthClient.enableUser(user.getEmail());

            if (user.getRole() == Role.COMPANY_MANAGER) {
                companyManagerRepository.findById(userId).ifPresent(cm -> {
                    if (cm.getCompanyId() != null) {
                        keycloakAuthClient.updateUserAttribute(
                                user.getEmail(), "company_id", cm.getCompanyId().toString()
                        );
                    }
                });
            }

        } else if (request.getStatus() == ApprovalStatus.REJECTED) {
            user.reject(adminId, request.getRejectedReason());
        } else {
            throw new BusinessException(UserErrorCode.INVALID_APPROVAL_STATUS);
        }
    }
}