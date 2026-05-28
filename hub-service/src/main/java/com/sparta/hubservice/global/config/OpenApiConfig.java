package com.sparta.hubservice.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hubServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hub Service API 명세서")
                        .description("Hub Service API documentation")
                        .version("v1.0.0"))
                .servers(List.of(new Server().url("/hub").description("Default Server URL through Gateway")));
    }
}
