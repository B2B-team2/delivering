package com.sparta.userservice.global.config.security.util;

import com.sparta.userservice.user.domain.enums.Role;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class SecurityUtils {

    /**
     * 요청 가져오기
     */
    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new RuntimeException("UNAUTHORIZED");
        }
        return attributes.getRequest();
    }

    /**
     * Keycloak userId (sub) — 게이트웨이에서 주입한 X-User-Id 헤더
     */
    public String getUserId() {
        String userId = getRequest().getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("UNAUTHORIZED");
        }
        return userId;
    }

    /**
     * email — 게이트웨이에서 주입한 X-User-Email 헤더
     */
    public String getEmail() {
        String email = getRequest().getHeader("X-User-Email");
        if (email == null || email.isBlank()) {
            throw new RuntimeException("UNAUTHORIZED");
        }
        return email;
    }

    /**
     * role — 게이트웨이에서 주입한 X-User-Role 헤더 → Role enum 변환
     */
    public List<Role> getRoles() {
        String role = getRequest().getHeader("X-User-Role");
        if (role == null || role.isBlank()) {
            return Collections.emptyList();
        }
        Role converted = toRole(role);
        return converted != null ? List.of(converted) : Collections.emptyList();
    }

    /**
     * 특정 role 보유 여부
     */
    public boolean hasRole(Role role) {
        return getRoles().contains(role);
    }

    /**
     * MASTER 체크
     */
    public boolean isNotMaster() {
        return !hasRole(Role.MASTER);
    }

    /**
     * HUB_MANAGER 체크
     */
    public boolean isNotHubManager() {
        return !hasRole(Role.HUB_MANAGER);
    }

    /**
     * HUB DELIVERY MANAGER 체크
     */
    public boolean isHubDeliveryManager() {
        return hasRole(Role.HUB_DELIVERY_MANAGER);
    }

    /**
     * COMPANY MANAGER 체크
     */
    public boolean isCompanyManager() {
        return hasRole(Role.COMPANY_MANAGER);
    }

    /**
     * COMPANY DELIVERY MANAGER 체크
     */
    public boolean isCompanyDeliveryManager() {
        return hasRole(Role.COMPANY_DELIVERY_MANAGER);
    }


    /**
     * company_id — 게이트웨이에서 주입한 X-Company-Id 헤더
     * COMPANY_MANAGER 전용. MASTER는 null 반환.
     */
    public UUID getCompanyId() {
        String companyId = getRequest().getHeader("X-Company-Id");
        if (companyId == null || companyId.isBlank()) {
            return null;
        }
        return UUID.fromString(companyId);
    }

    /**
     * String → Role 변환 (안전 처리)
     */
    private Role toRole(String role) {
        try {
            return Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * hub_id — 게이트웨이에서 주입한 X-Hub-Id 헤더
     * HUB_MANAGER 전용. MASTER는 null 반환.
     */
    public UUID getHubId() {
        String hubId = getRequest().getHeader("X-Hub-Id");
        if (hubId == null || hubId.isBlank()) {
            return null;
        }
        return UUID.fromString(hubId);
    }
}