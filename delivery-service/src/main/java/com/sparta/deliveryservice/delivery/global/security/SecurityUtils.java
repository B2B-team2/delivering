package com.sparta.deliveryservice.delivery.global.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

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

    public String getRole() {
        String value = getRequest().getHeader("X-User-Role");
        return value != null ? value : "";
    }


    public boolean isMaster() {
        return "MASTER".equals(getRole());
    }

    public boolean isHubManager() {
        return "HUB_MANAGER".equals(getRole());
    }

    public boolean isHubDeliveryManager() {
        return "HUB_DELIVERY_MANAGER".equals(getRole());
    }

    public boolean isCompanyDeliveryManager() {
        return "COMPANY_DELIVERY_MANAGER".equals(getRole());
    }

    public boolean  canUpdate(UUID deliveryManagerId) {
        if (isMaster() || isHubManager()) return true;

        if (isHubDeliveryManager() || isCompanyDeliveryManager()) {
            return deliveryManagerId != null && getUserId().equals(deliveryManagerId);
        }
        return false;
    }

    public boolean canDelete() {
        return isMaster() || isHubManager();
    }
}