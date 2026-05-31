package com.sparta.orderservice.global.security;

import com.sparta.common.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityUtils implements AuthContext {

    private CustomUserDetails getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails)) return null;
        return (CustomUserDetails) auth.getPrincipal();
    }

    public UUID getCompanyId() {
        CustomUserDetails user = getCurrentUser();
        if (user == null || user.getCompanyId() == null) return null;
        return UUID.fromString(user.getCompanyId());
    }

    public boolean isMaster() {
        CustomUserDetails user = getCurrentUser();
        return user != null && "MASTER".equals(user.getRole());
    }

    public boolean isCompanyManager() {
        CustomUserDetails user = getCurrentUser();
        return user != null && "COMPANY_MANAGER".equals(user.getRole());
    }
}
