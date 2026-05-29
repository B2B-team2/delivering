package com.sparta.companyservice.integration.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional // 테스트 완료 후 자동 롤백으로 데이터 정합성 유지
public abstract class IntegrationTestSupport {
    // H2를 사용하여 인메모리 테스트 환경으로 전환 (Docker 의존성 제거)
}
