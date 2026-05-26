package com.sparta.hubservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    private static final UUID SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Bean
    public AuditorAware<UUID> auditorProvider() {
        return () -> {
            try {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs == null) return Optional.of(SYSTEM_UUID);
                String userId = attrs.getRequest().getHeader("X-User-Id");
                if (userId == null || userId.isBlank()) return Optional.of(SYSTEM_UUID);
                return Optional.of(UUID.fromString(userId));
            } catch (Exception e) {
                return Optional.of(SYSTEM_UUID);
            }
        };
    }
}
