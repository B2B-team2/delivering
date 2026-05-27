package com.sparta.deliveryservice.deliveryLog.global.security;

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

    public String getRole() {
        String value = getRequest().getHeader("X-User-Role");
        return value != null ? value : "";
    }

    public boolean isMaster() {
        return "MASTER".equals(getRole());
    }


}