package com.sparta.companyservice.product.infrastructure.adapter;

import com.sparta.companyservice.company.application.service.CompanyService;
import com.sparta.companyservice.product.application.port.CompanyQueryPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CompanyQueryAdapter implements CompanyQueryPort {

    private final CompanyService companyService;

    @Override
    @CircuitBreaker(name = "companyCircuitBreaker", fallbackMethod = "existsCompanyFallback")
    public boolean existsCompanyById(UUID companyId) {
        return companyService.existsCompany(companyId);
    }

    // Fallback: 업체 서비스 장애 시 안전을 위해 일단 존재한다고 가정하거나 로직에 따라 처리
    // 여기서는 조회의 흐름을 끊지 않기 위해 기본 true 반환 (또는 false 후 에러 처리)
    private boolean existsCompanyFallback(UUID companyId, Throwable t) {
        log.error("CircuitBreaker 'companyCircuitBreaker' triggered for companyId: {}. Reason: {}", companyId, t.getMessage());
        return true; 
    }
}
