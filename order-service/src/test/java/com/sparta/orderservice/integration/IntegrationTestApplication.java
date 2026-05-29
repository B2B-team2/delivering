package com.sparta.orderservice.integration;

import com.sparta.orderservice.OrderServiceApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 통합 테스트 전용 Spring Boot 애플리케이션 진입점
 *
 * @SpringBootApplication(scanBasePackages="com.sparta.orderservice") 대신
 * 어노테이션을 분리하여 @ComponentScan에 excludeFilters를 추가함.
 *
 * 이유: com.sparta.orderservice 스캔 시 OrderServiceApplication이 @Configuration으로 처리되어
 * 프로덕션 scan("com.sparta")가 실행 → common.GlobalExceptionHandler와 빈 이름 충돌.
 * → OrderServiceApplication을 exclude하여 재스캔 차단.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@EnableJpaAuditing
@EnableFeignClients(basePackages = "com.sparta.orderservice")
@ComponentScan(
        basePackages = "com.sparta.orderservice",
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = OrderServiceApplication.class
        )
)
public class IntegrationTestApplication {
}
