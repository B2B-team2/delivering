package com.sparta.deliveryservice.delivery.application.service;

import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.presentation.dto.resqonse.DeliveryCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryLockFacade {

    private final RedissonClient redissonClient;
    private final DeliveryService deliveryService;

    public List<DeliveryCreateResponse> createDeliveriesWithLock(List<DeliveryCreateClientRequest> requests) {
        List<DeliveryCreateResponse> totalResponses = new ArrayList<>();

        for (DeliveryCreateClientRequest request : requests) {
            String lockKey = "lock:order:" + request.getCompanyOrderId().toString();
            RLock lock = redissonClient.getLock(lockKey);

            try {
                boolean available = lock.tryLock(5, 3, TimeUnit.SECONDS);

                if (!available) {
                    log.warn("락 획득 실패 - 이미 처리 중인 주문 ID입니다: {}", request.getCompanyOrderId());
                    throw new IllegalStateException("현재 처리 중인 주문입니다. 잠시 후 다시 시도해 주세요.");
                }

                DeliveryCreateResponse response = deliveryService.createSingleDelivery(request);
                totalResponses.add(response);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("락 획득 중 인터럽트 발생", e);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        return totalResponses;
    }
}