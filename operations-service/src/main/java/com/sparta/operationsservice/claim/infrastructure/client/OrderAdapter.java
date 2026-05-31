package com.sparta.operationsservice.claim.infrastructure.client;

import com.sparta.operationsservice.claim.application.port.OrderPort;
import com.sparta.operationsservice.claim.infrastructure.client.dto.CompanyOrderDetailsResponse;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAdapter implements OrderPort {

    private final OrderClient orderClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    @Override
    public CompanyOrderDetails getCompanyOrderDetails(UUID companyOrderId) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker");
        Retry retry = retryRegistry.retry("claimRetry");

        log.info("[Resilience] Executing Supplier - CB State: {}", circuitBreaker.getState());
        
        return circuitBreaker.executeSupplier(() -> 
            retry.executeSupplier(() -> {
                log.info("[Resilience] Real Feign Call to OrderClient");
                CompanyOrderDetailsResponse response = orderClient.getCompanyOrderDetails(companyOrderId);
                return new CompanyOrderDetails(
                        response.orderId(),
                        response.companyOrderId(),
                        response.items().stream()
                                .map(item -> new CompanyOrderDetails.ItemDetails(item.productOptionId(), item.quantity()))
                                .collect(Collectors.toList())
                );
            })
        );
    }

    @Override
    public void cancelCompanyOrderByClaim(UUID companyOrderId, UUID adminId) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("claimCircuitBreaker");
        Retry retry = retryRegistry.retry("claimRetry");

        log.info("[Resilience] Executing Runnable - CB State: {}", circuitBreaker.getState());

        circuitBreaker.executeRunnable(() -> 
            retry.executeRunnable(() -> 
                orderClient.cancelCompanyOrderByClaim(companyOrderId, adminId)
            )
        );
    }
}
