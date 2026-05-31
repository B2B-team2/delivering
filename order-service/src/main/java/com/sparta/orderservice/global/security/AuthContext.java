package com.sparta.orderservice.global.security;

import java.util.UUID;

// 게이트웨이가 주입한 인증 정보 — HTTP 구현체(SecurityUtils)를 application 계층에서 직접 참조하지 않도록 추상화
public interface AuthContext {
    boolean isMaster();
    boolean isCompanyManager();
    UUID getCompanyId();
}
