package com.sparta.hubservice.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                ServletRequestAttributes attrs =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attrs == null) return Optional.of("system");
                String username = attrs.getRequest().getHeader("X-User-Name");
                return Optional.ofNullable(username).filter(s -> !s.isBlank()).or(() -> Optional.of("system"));
            } catch (Exception e) {
                return Optional.of("system");
            }
        };
    }
}
