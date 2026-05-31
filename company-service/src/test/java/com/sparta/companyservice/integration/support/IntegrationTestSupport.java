package com.sparta.companyservice.integration.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestSupport {
    static {
        // 보안 필터 통과를 위한 시스템 프로퍼티 설정
        System.setProperty("GATEWAY_SECRET", "local-secret");
    }
}
