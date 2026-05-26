package com.sparta.userservice.auth.application.service;

import com.sparta.userservice.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final RedisService redisService;

    private static final String BLACKLIST_PREFIX = "user:blacklist:access:";
    private static final String REFRESH_PREFIX = "user:token:";

    // Refresh Token Redis 저장 (login 시 재사용)
    public void saveRefreshToken(UUID userId, String refreshToken) {
        redisService.set(
                REFRESH_PREFIX + userId,
                refreshToken,
                Duration.ofDays(7)
        );
    }

    // Redis 저장 Refresh Token 조회
    public String getRefreshToken(UUID userId) {
        return redisService.get(REFRESH_PREFIX + userId);
    }

    // Refresh Token Redis 삭제
    public void deleteRefreshToken(UUID userId) {
        redisService.delete(REFRESH_PREFIX + userId);
    }

    // Access Token 블랙리스트 등록 (logout 시 재사용)
    public void blacklistAccessToken(String accessToken) {
        try {
            redisService.set(
                    BLACKLIST_PREFIX + accessToken,
                    "logout",
                    Duration.ofHours(1)
            );
        } catch (Exception e) {
            log.warn("[AUTH] Access Token 블랙리스트 등록 실패 - {}", e.getMessage());
        }
    }

    // Access Token 블랙리스트 여부 확인
    public boolean isBlacklisted(String accessToken) {
        return redisService.exists(BLACKLIST_PREFIX + accessToken);
    }
}