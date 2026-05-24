package com.sparta.operationsservice.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI operationsServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Operations Service API 명세서")
                        .description("Operations Service API documentation")
                        .version("v1.0.0"))
                .servers(List.of(new Server().url("/operations").description("Default Server URL through Gateway")));
    }
}
