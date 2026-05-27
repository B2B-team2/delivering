package com.sparta.operationsservice.global.application.service;

import com.sparta.common.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("authService")
public class AuthService {

    public boolean isClaimOwner(UUID claimId) {
        // TODO: 향후 order-service와 연동하여 실제 주문의 소유권을 확인할 때 구현 예정
        return true;
    }

    private CustomUserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
            return null;
        }
        return (CustomUserDetails) authentication.getPrincipal();
    }
}
