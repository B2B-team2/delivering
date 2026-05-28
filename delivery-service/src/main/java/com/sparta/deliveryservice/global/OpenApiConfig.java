package com.sparta.deliveryservice.global;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;


@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI deliveryServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Delivery Service API 명세서")
                        .description("Delivery Service API documentation")
                        .version("v1.0.0"))
                .servers(List.of(new Server().url("/delivery").description("Default Server URL through Gateway")));
    }
}
