package com.sparta.operationsservice.global.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayFeignInterceptor {

    @Bean
    public RequestInterceptor gatewaySecretInterceptor() {
        return template -> {
            String secret = System.getenv().getOrDefault("GATEWAY_SECRET", "local-secret");
            template.header("X-Gateway-Secret", secret);
        };
    }
}
