package com.sparta.apigateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtDecoder jwtDecoder;

    private static final List<String> WHITELIST = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/refresh",
            "/swagger-ui",
            "/v3/api-docs",
            "/user/v3/api-docs",
            "/company/v3/api-docs",
            "/order/v3/api-docs",
            "/delivery/v3/api-docs",
            "/hub/v3/api-docs",
            "/operations/v3/api-docs",
            "/actuator"
    );

    public JwtAuthenticationFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Jwt jwt = jwtDecoder.decode(token);

            String userId = jwt.getSubject();
            String email = jwt.getClaimAsString("preferred_username");
            String role = extractRole(jwt);
            final String finalRole = role != null ? role : "";

            ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(super.getHeaders());
                    headers.set("X-User-Id", userId);
                    headers.set("X-User-Email", email);
                    headers.set("X-User-Role", finalRole);
                    String companyId = jwt.getClaimAsString("company_id");
                    if (companyId != null) {
                        headers.set("X-Company-Id", companyId);
                    }
                    return headers;
                }
            };

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException e) {
            log.warn("[Gateway] JWT 검증 실패: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private String extractRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return null;

        Object rolesObj = realmAccess.get("roles");
        if (!(rolesObj instanceof List<?> roles)) return null;

        List<String> systemRoles = List.of(
                "MASTER", "HUB_MANAGER", "HUB_DELIVERY_MANAGER",
                "COMPANY_DELIVERY_MANAGER", "COMPANY_MANAGER"
        );

        return roles.stream()
                .map(Object::toString)
                .filter(systemRoles::contains)
                .findFirst()
                .orElse(null);
    }

    private boolean isWhitelisted(String path) {
        return WHITELIST.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return -1;
    }
}