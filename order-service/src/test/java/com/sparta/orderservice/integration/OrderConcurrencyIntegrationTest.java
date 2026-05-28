package com.sparta.orderservice.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.security.AuthContext;
import com.sparta.orderservice.order.application.service.OrderCommandService;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * 주문 서비스 동시성 통합 테스트
 *
 * 목적:
 * - 여러 스레드가 동시에 주문 취소를 요청할 때 Saga 보상 스택이 스레드별로 독립되는지 경험적으로 검증
 * - OrderCommandService의 보상 스택(Deque)은 메서드 로컬 변수이므로 스레드 간 공유되지 않음
 *
 * 한계(현재 구조의 검증 범위):
 * - Repository 전체가 @MockBean → DB 레벨 경쟁 조건(낙관적 락, 데드락)은 검증하지 않음
 * - "서비스 레이어 스레드 안전성 + WireMock 동시 요청 처리"만 검증
 *
 * CountDownLatch(startLatch) 패턴으로 모든 스레드를 동시에 출발시켜 경쟁 조건 유발을 극대화한다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = IntegrationTestApplication.class)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("integration-test")
@DisplayName("주문 취소 동시성 통합 테스트")
class OrderConcurrencyIntegrationTest {

    @MockBean private OrderRepository orderRepository;
    @MockBean private CompanyOrderRepository companyOrderRepository;
    @MockBean private PaymentRepository paymentRepository;
    @MockBean private DraftRepository draftRepository;
    @MockBean private AuthContext authContext;

    @Autowired private OrderCommandService orderCommandService;

    private static final UUID RECEIVER_COMPANY_ID = UUID.fromString("cccc3333-0000-0000-0000-000000000001");
    private static final UUID SUPPLIER_COMPANY_ID = UUID.fromString("cccc3333-0000-0000-0000-000000000002");
    private static final UUID REQUESTER_ID        = UUID.fromString("eeee3333-0000-0000-0000-000000000001");
    /** 동시 스레드 수: DB 없이 WireMock만 사용하므로 30개로 충분한 경쟁 조건 유발 */
    private static final int  THREAD_COUNT        = 30;

    @BeforeEach
    void setUp() {
        WireMock.reset();
        lenient().when(authContext.isCompanyManager()).thenReturn(false);
        lenient().when(authContext.isMaster()).thenReturn(true);
        lenient().when(paymentRepository.findPaymentByOrderId(any())).thenReturn(Optional.empty());
    }

    // ── 도메인 객체 생성 헬퍼 ─────────────────────────────────────────────

    /**
     * 각 호출마다 독립된 UUID를 가진 Order + CompanyOrder 생성.
     * 스레드 간 상태 공유를 방지하기 위해 호출마다 새 인스턴스를 반환한다.
     */
    private Order buildCancellableOrder() {
        Order order = Order.of(
                RECEIVER_COMPANY_ID, "홍길동", "010-0000-0000", null,
                "{\"address\":\"서울시 강남구\"}", LocalDateTime.now().plusDays(7), null,
                BigDecimal.valueOf(20000), BigDecimal.ZERO, BigDecimal.valueOf(20000)
        );
        order.addCompanyOrder(
                CompanyOrder.of(order, SUPPLIER_COMPANY_ID, BigDecimal.valueOf(20000), BigDecimal.ZERO)
        );
        return order;
    }

    // ── 동시 실행 헬퍼 ────────────────────────────────────────────────────

    /**
     * tasks 목록을 THREAD_COUNT 크기 스레드 풀로 동시에 실행하고 결과를 반환한다.
     *
     * <ul>
     *   <li>startLatch(1): 모든 스레드가 준비된 후 동시 출발 → 경쟁 조건 극대화</li>
     *   <li>BusinessException: 예상된 도메인 실패로 분류 (failCount)</li>
     *   <li>그 외 예외: 예상치 못한 오류로 분류 (unexpectedErrors) → 테스트 실패 원인</li>
     * </ul>
     */
    private ConcurrentResult runConcurrently(List<Runnable> tasks) throws InterruptedException {
        int count = tasks.size();
        ExecutorService executor = Executors.newFixedThreadPool(count);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch  = new CountDownLatch(count);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount    = new AtomicInteger();
        List<Throwable> unexpected = Collections.synchronizedList(new ArrayList<>());

        tasks.forEach(task -> executor.submit(() -> {
            try {
                startLatch.await();   // 모든 스레드가 준비될 때까지 대기
                task.run();
                successCount.incrementAndGet();
            } catch (BusinessException e) {
                failCount.incrementAndGet();  // 예상된 도메인 예외 (취소 실패 등)
            } catch (Throwable t) {
                unexpected.add(t);            // 예상치 못한 오류 (스레드 안전성 위반 등)
            } finally {
                doneLatch.countDown();
            }
        }));

        startLatch.countDown();  // 모든 스레드 동시 출발
        assertThat(doneLatch.await(30, TimeUnit.SECONDS))
                .as("30초 내 모든 스레드가 완료되어야 함").isTrue();
        executor.shutdown();

        return new ConcurrentResult(successCount.get(), failCount.get(), unexpected);
    }

    record ConcurrentResult(int successCount, int failCount, List<Throwable> unexpectedErrors) {}

    // ══════════════════════════════════════════════════════════════════════
    // cancelOrder 동시성
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelOrder 동시성")
    class CancelOrderConcurrency {

        @Test
        @DisplayName("N개 주문 동시 취소 성공 — 각 스레드가 독립적으로 hub·delivery cancel 1회씩 호출")
        void concurrent_cancel_all_succeed() throws InterruptedException {
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel")).willReturn(ok()));
            stubFor(post(urlEqualTo("/api/v1/internal/deliveries/cancel")).willReturn(ok()));

            // N개의 독립된 Order 준비 (각각 다른 orderId)
            List<Order> orders = IntStream.range(0, THREAD_COUNT)
                    .mapToObj(i -> buildCancellableOrder())
                    .toList();
            orders.forEach(o -> lenient().when(orderRepository.findOrderById(o.getOrderId()))
                    .thenReturn(Optional.of(o)));

            ConcurrentResult result = runConcurrently(
                    orders.stream()
                          .map(o -> (Runnable) () -> orderCommandService.cancelOrder(o.getOrderId(), REQUESTER_ID))
                          .toList()
            );

            assertThat(result.unexpectedErrors()).isEmpty();
            assertThat(result.successCount()).isEqualTo(THREAD_COUNT);
            // 스레드마다 독립적으로 외부 서비스 1회 호출 → 총 THREAD_COUNT회
            verify(THREAD_COUNT, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(THREAD_COUNT, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
            // 성공 경로 → 보상 없음
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
        }

        @Test
        @DisplayName("delivery cancel 동시 실패 → Saga 보상 스택 스레드 독립성: 각 스레드가 정확히 1회씩 hub 재예약")
        void concurrent_cancel_delivery_fails_compensation_is_thread_isolated() throws InterruptedException {
            /*
             * [핵심 검증]
             * cancelOrder 내부의 보상 스택: Deque<Runnable> compensations = new ArrayDeque<>()
             * → 메서드 로컬 변수이므로 각 스레드 호출마다 독립적으로 생성됨.
             *
             * 시나리오:
             * 1. hub cancel    → 성공 (30회)
             * 2. delivery cancel → 500 실패 (30회)
             * 3. catch 블록: 보상 스택 실행 → hub 재예약 (30회)
             *
             * 보상 스택이 스레드 간 공유됐다면:
             * - hub 재예약 횟수가 30이 아니거나
             * - ConcurrentModificationException이 unexpectedErrors에 수집됨
             */
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel")).willReturn(ok()));
            stubFor(post(urlEqualTo("/api/v1/internal/deliveries/cancel")).willReturn(serverError()));
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/reserve")).willReturn(ok()));

            List<Order> orders = IntStream.range(0, THREAD_COUNT)
                    .mapToObj(i -> buildCancellableOrder())
                    .toList();
            orders.forEach(o -> lenient().when(orderRepository.findOrderById(o.getOrderId()))
                    .thenReturn(Optional.of(o)));

            ConcurrentResult result = runConcurrently(
                    orders.stream()
                          .map(o -> (Runnable) () -> orderCommandService.cancelOrder(o.getOrderId(), REQUESTER_ID))
                          .toList()
            );

            assertThat(result.unexpectedErrors()).isEmpty();
            // 전체 실패 (DELIVERY_SERVICE_UNAVAILABLE)
            assertThat(result.failCount()).isEqualTo(THREAD_COUNT);
            // hub cancel: 30회 (1단계 성공)
            verify(THREAD_COUNT, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            // 핵심: 스레드별 독립 보상 → 정확히 THREAD_COUNT회 hub 재예약
            verify(THREAD_COUNT, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
        }

        @Test
        @DisplayName("hub cancel 동시 실패 → 첫 단계 실패이므로 보상 없이 즉시 종료 (스레드별 독립)")
        void concurrent_cancel_hub_fails_no_compensation() throws InterruptedException {
            /*
             * cancelOrder Saga 1단계(hub cancel)가 실패하면
             * 보상 스택에는 아무것도 쌓이지 않은 상태 → 보상 호출 없음.
             * 30개 스레드 모두 동일한 결과여야 한다.
             */
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel")).willReturn(serverError()));

            List<Order> orders = IntStream.range(0, THREAD_COUNT)
                    .mapToObj(i -> buildCancellableOrder())
                    .toList();
            orders.forEach(o -> lenient().when(orderRepository.findOrderById(o.getOrderId()))
                    .thenReturn(Optional.of(o)));

            ConcurrentResult result = runConcurrently(
                    orders.stream()
                          .map(o -> (Runnable) () -> orderCommandService.cancelOrder(o.getOrderId(), REQUESTER_ID))
                          .toList()
            );

            assertThat(result.unexpectedErrors()).isEmpty();
            assertThat(result.failCount()).isEqualTo(THREAD_COUNT);
            // 1단계 실패 → delivery cancel·hub 재예약 모두 미호출
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // cancelCompanyOrder 동시성
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelCompanyOrder 동시성")
    class CancelCompanyOrderConcurrency {

        @Test
        @DisplayName("N개 서브주문 동시 취소 성공 — 각 스레드가 독립적으로 hub company cancel 1회씩 호출")
        void concurrent_company_cancel_all_succeed() throws InterruptedException {
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel/company")).willReturn(ok()));

            // N개의 독립된 CompanyOrder 준비
            List<CompanyOrder> companyOrders = IntStream.range(0, THREAD_COUNT)
                    .mapToObj(i -> buildCancellableOrder().getCompanyOrders().get(0))
                    .toList();
            companyOrders.forEach(co ->
                    lenient().when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                            .thenReturn(Optional.of(co)));

            ConcurrentResult result = runConcurrently(
                    companyOrders.stream()
                                 .map(co -> (Runnable) () ->
                                         orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), REQUESTER_ID))
                                 .toList()
            );

            assertThat(result.unexpectedErrors()).isEmpty();
            assertThat(result.successCount()).isEqualTo(THREAD_COUNT);
            verify(THREAD_COUNT, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel/company")));
            // cancelCompanyOrder는 delivery cancel을 호출하지 않음
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
        }
    }
}
