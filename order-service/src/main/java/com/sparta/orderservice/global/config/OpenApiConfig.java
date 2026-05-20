package com.sparta.orderservice.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API 명세서")
                        .description("Order Service API documentation")
                        .version("v1.0.0"))
                .servers(List.of(new Server().url("/order").description("Default Server URL through Gateway")));
    }
}
