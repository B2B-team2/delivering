package com.sparta.deliveryservice.delivery.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.common.dto.BusinessException;
import com.sparta.deliveryservice.delivery.domain.core.Delivery;
import com.sparta.deliveryservice.delivery.domain.core.DeliveryAddress;
import com.sparta.deliveryservice.delivery.domain.repository.DeliveryRepository;
import com.sparta.deliveryservice.delivery.global.security.SecurityUtils;
import com.sparta.deliveryservice.delivery.infrastructure.client.CachedHubServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.DeliveryOrderServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.DeliveryUserServiceClient;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryCreateClientRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.request.DeliveryHubRouteSearchRequest;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryHubRouteSearchResponse;
import com.sparta.deliveryservice.delivery.infrastructure.client.dto.response.DeliveryManagerResponse;
import com.sparta.deliveryservice.deliveryLog.domin.repository.DeliveryLogRepository;
import com.sparta.deliveryservice.deliveryRoute.domain.repository.DeliveryRouteRepository;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.cache.CacheManager;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 같은 companyOrderId로 N건 동시 배송 생성 요청 시 실제로 몇 건이 중복 생성되는지 검증.
 * DB existsByCompanyOrderId 체크를 실제 DB 왕복처럼 약간의 지연을 준 Fake로 대체해
 * check-then-act 경쟁 조건을 실제로 재현한다(순수 mock의 정적 stub으로는 경쟁이 안 생김).
 *
 * 1) 락 없이 DeliveryService.createSingleDelivery()를 직접 동시 호출 -> 중복 발생 여부 확인
 * 2) DeliveryLockFacade.createDeliveriesWithLock()으로 동시 호출 -> 락 적용 후 결과 확인
 */
class DeliveryDuplicateCreationTest {

    private static final int CONCURRENCY = Integer.parseInt(
            System.getenv().getOrDefault("CONCURRENCY", "20"));

    @Test
    void 락_없이_동시_생성하면_중복이_발생한다() throws InterruptedException {
        Set<UUID> fakeDb = ConcurrentHashMap.newKeySet();
        DeliveryService deliveryService = buildDeliveryServiceWithFakeDb(fakeDb);
        UUID companyOrderId = UUID.randomUUID();

        Result result = runConcurrently(CONCURRENCY, () -> deliveryService.createSingleDelivery(sampleRequest(companyOrderId)));

        System.out.println("\n===== [락 없음] 동시 생성 " + CONCURRENCY + "건 결과 =====");
        System.out.println("성공(중복 생성됨): " + result.success.get() + "건, 정상 거부: " + result.rejected.get() + "건");
    }

    @Test
    void Redisson_락을_적용하면_중복이_방지된다() throws InterruptedException {
        Set<UUID> fakeDb = ConcurrentHashMap.newKeySet();
        DeliveryService deliveryService = buildDeliveryServiceWithFakeDb(fakeDb);

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://127.0.0.1:26379")
                .setPassword("logitech_secret_pass_2026!")
                .setConnectionPoolSize(Math.max(64, CONCURRENCY + 16))
                .setConnectionMinimumIdleSize(Math.min(32, CONCURRENCY));
        RedissonClient redissonClient = Redisson.create(config);
        DeliveryLockFacade lockFacade = new DeliveryLockFacade(redissonClient, deliveryService);

        UUID companyOrderId = UUID.randomUUID();

        Result result = runConcurrently(CONCURRENCY, () ->
                lockFacade.createDeliveriesWithLock(java.util.List.of(sampleRequest(companyOrderId))));

        System.out.println("\n===== [Redisson 락 적용] 동시 생성 " + CONCURRENCY + "건 결과 =====");
        System.out.println("성공(생성됨): " + result.success.get() + "건, 정상 거부(락/중복): " + result.rejected.get() + "건");

        redissonClient.shutdown();
    }

    private DeliveryService buildDeliveryServiceWithFakeDb(Set<UUID> fakeDb) {
        DeliveryRepository deliveryRepository = mock(DeliveryRepository.class);
        DeliveryRouteRepository deliveryRouteRepository = mock(DeliveryRouteRepository.class);
        DeliveryLogRepository deliveryLogRepository = mock(DeliveryLogRepository.class);
        CachedHubServiceClient cachedHubServiceClient = mock(CachedHubServiceClient.class);
        DeliveryUserServiceClient deliveryUserServiceClient = mock(DeliveryUserServiceClient.class);
        DeliveryOrderServiceClient deliveryOrderServiceClient = mock(DeliveryOrderServiceClient.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CacheManager cacheManager = mock(CacheManager.class);
        SecurityUtils securityUtils = mock(SecurityUtils.class);
        DeliverySlackNotificationService deliverySlackNotificationService = mock(DeliverySlackNotificationService.class);

        // 실제 DB 존재 체크를 흉내낸 Fake: 약간의 지연을 둬서 check-then-act 경쟁을 실제로 재현
        when(deliveryRepository.existsByCompanyOrderId(any())).thenAnswer(invocation -> {
            UUID id = invocation.getArgument(0);
            Thread.sleep(30);
            return fakeDb.contains(id);
        });
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            fakeDb.add(d.getCompanyOrderId());
            return d;
        });

        DeliveryHubRouteSearchResponse.HubRouteDto routeDto = DeliveryHubRouteSearchResponse.HubRouteDto.builder()
                .sequence(1)
                .fromHubId(UUID.randomUUID())
                .fromHubName("서울허브")
                .toHubId(UUID.randomUUID())
                .toHubName("부산허브")
                .build();
        DeliveryHubRouteSearchResponse hubResponse = DeliveryHubRouteSearchResponse.builder()
                .routes(java.util.List.of(routeDto))
                .build();
        when(cachedHubServiceClient.getHubRouteWithCache(any(DeliveryHubRouteSearchRequest.class)))
                .thenReturn(hubResponse);

        when(deliveryUserServiceClient.getManagerInfo(any())).thenReturn(
                DeliveryManagerResponse.builder()
                        .deliveryManagerId(UUID.randomUUID())
                        .deliverySlackId("U000")
                        .managerName("테스트매니저")
                        .managerPhone("010-0000-0000")
                        .build());

        return new DeliveryService(deliveryRepository, deliveryRouteRepository, deliveryLogRepository,
                cachedHubServiceClient, deliveryUserServiceClient, deliveryOrderServiceClient,
                objectMapper, cacheManager, securityUtils, deliverySlackNotificationService);
    }

    private DeliveryCreateClientRequest sampleRequest(UUID companyOrderId) {
        return DeliveryCreateClientRequest.builder()
                .addressId(UUID.randomUUID())
                .companyOrderId(companyOrderId)
                .companyReceiveId(UUID.randomUUID())
                .departureHubId(UUID.randomUUID())
                .destinationHubId(UUID.randomUUID())
                .deliveryAddress(DeliveryAddress.builder().address("테스트 주소").addressDetail("101호").build())
                .phone("010-1234-5678")
                .postalCode("12345")
                .recipientName("홍길동")
                .recipientSlackId("U123")
                .memo("동시성 테스트")
                .build();
    }

    private Result runConcurrently(int concurrency, ThrowingRunnable action) throws InterruptedException {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        CountDownLatch ready = new CountDownLatch(concurrency);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(concurrency);
        AtomicInteger success = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < concurrency; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    action.run();
                    success.incrementAndGet();
                } catch (BusinessException | IllegalStateException e) {
                    rejected.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("예상 못한 예외: " + e);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await(120, TimeUnit.SECONDS);
        pool.shutdown();

        return new Result(success, rejected);
    }

    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record Result(AtomicInteger success, AtomicInteger rejected) {
    }
}
