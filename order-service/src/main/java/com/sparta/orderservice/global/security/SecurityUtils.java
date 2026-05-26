package com.sparta.orderservice.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

// 게이트웨이가 주입한 인증 헤더를 읽는 유틸리티
@Component
public class SecurityUtils {

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) throw new IllegalStateException("HTTP 요청 컨텍스트가 없습니다.");
        return attributes.getRequest();
    }

    public UUID getUserId() {
        String value = getRequest().getHeader("X-User-Id");
        if (value == null || value.isBlank()) throw new IllegalStateException("X-User-Id 헤더가 없습니다.");
        return UUID.fromString(value);
    }

    // X-User-Role: MASTER, HUB_MANAGER | HUB_DELIVERY_MANAGER, COMPANY_DELIVERY_MANAGER | COMPANY_MANAGER
    public String getRole() {
        String value = getRequest().getHeader("X-User-Role");
        return value != null ? value : "";
    }

    // X-Company-Id: COMPANY_MANAGER에게만 게이트웨이가 주입 (그 외 역할은 null)
    public UUID getCompanyId() {
        String value = getRequest().getHeader("X-Company-Id");
        if (value == null || value.isBlank()) return null;
        return UUID.fromString(value);
    }

    public boolean isMaster() {
        return "MASTER".equals(getRole());
    }

    public boolean isCompanyManager() {
        return "COMPANY_MANAGER".equals(getRole());
    }
}
