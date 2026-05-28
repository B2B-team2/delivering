package com.sparta.orderservice.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.OrderErrorCode;
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
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * 주문·서브주문 취소 Saga 통합 테스트
 *
 * 커버리지:
 * - OrderCommandService.cancelOrder       : hub cancel → delivery cancel → 보상 트랜잭션
 * - OrderCommandService.cancelCompanyOrder : hub companyStock cancel → 보상 트랜잭션
 *
 * WireMock으로 hub-service / delivery-service 외부 HTTP 호출을 스텁합니다.
 * 레포지토리는 @MockBean으로 DB 의존성을 제거합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = IntegrationTestApplication.class)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("integration-test")
@DisplayName("주문·서브주문 취소 Saga 통합 테스트")
class OrderCancelIntegrationTest {

    @MockBean private OrderRepository orderRepository;
    @MockBean private CompanyOrderRepository companyOrderRepository;
    @MockBean private PaymentRepository paymentRepository;
    @MockBean private DraftRepository draftRepository;
    @MockBean private AuthContext authContext;

    @Autowired private OrderCommandService orderCommandService;

    private static final UUID RECEIVER_COMPANY_ID = UUID.fromString("aaaa1111-0000-0000-0000-000000000001");
    private static final UUID SUPPLIER_COMPANY_ID = UUID.fromString("aaaa1111-0000-0000-0000-000000000002");
    private static final UUID REQUESTER_ID        = UUID.fromString("dddd1111-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        WireMock.reset();
        // 기본 인증 컨텍스트: MASTER 권한 → 모든 주문 취소 가능
        lenient().when(authContext.isCompanyManager()).thenReturn(false);
        lenient().when(authContext.isMaster()).thenReturn(true);
        // 결제 취소 이벤트(PaymentEventHandler) → paymentRepository.findPaymentByOrderId → empty 반환
        // ifPresent 블록이 실행되지 않아 별도 payment 설정 불필요
        lenient().when(paymentRepository.findPaymentByOrderId(any())).thenReturn(Optional.empty());
    }

    // ── 도메인 객체 생성 헬퍼 ─────────────────────────────────────────────

    /**
     * 취소 가능 상태(PENDING, CompanyOrder: ORDERED)인 Order 생성
     * Order.orderId, CompanyOrder.companyOrderId는 내부에서 UUID.randomUUID()로 자동 생성
     */
    private Order buildCancellableOrder() {
        Order order = Order.of(
                RECEIVER_COMPANY_ID, "홍길동", "010-0000-0000", null,
                "{\"address\":\"서울시 강남구\"}", LocalDateTime.now().plusDays(7), null,
                BigDecimal.valueOf(20000), BigDecimal.ZERO, BigDecimal.valueOf(20000)
        );
        CompanyOrder co = CompanyOrder.of(
                order, SUPPLIER_COMPANY_ID, BigDecimal.valueOf(20000), BigDecimal.ZERO
        );
        order.addCompanyOrder(co);
        return order;
    }

    // ── WireMock stub 헬퍼 ───────────────────────────────────────────────

    private void stubHubCancelSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel")).willReturn(ok()));
    }

    private void stubHubReserveSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/inventory/reserve")).willReturn(ok()));
    }

    private void stubDeliveryCancelSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/deliveries/cancel")).willReturn(ok()));
    }

    private void stubHubCompanyStockCancelSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel/company")).willReturn(ok()));
    }

    // ══════════════════════════════════════════════════════════════════════
    // cancelOrder
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelOrder — 주문 전체 취소")
    class CancelOrder {

        @Test
        @DisplayName("정상: hub cancelStock → delivery cancelDeliveries 순으로 HTTP 호출")
        void success_calls_hub_cancel_then_delivery_cancel() {
            Order order = buildCancellableOrder();
            lenient().when(orderRepository.findOrderById(order.getOrderId()))
                    .thenReturn(Optional.of(order));
            stubHubCancelSuccess();
            stubDeliveryCancelSuccess();

            orderCommandService.cancelOrder(order.getOrderId(), REQUESTER_ID);

            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
            // 보상 호출 없음
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
        }

        @Test
        @DisplayName("hub cancelStock 500 → HUB_SERVICE_UNAVAILABLE, delivery cancel 미호출, 보상 없음")
        void hub_cancel_500_no_delivery_no_compensation() {
            Order order = buildCancellableOrder();
            lenient().when(orderRepository.findOrderById(order.getOrderId()))
                    .thenReturn(Optional.of(order));
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel")).willReturn(serverError()));

            assertThatThrownBy(() -> orderCommandService.cancelOrder(order.getOrderId(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.HUB_SERVICE_UNAVAILABLE));

            // hub 취소가 첫 번째 단계 → 보상 스택에 아무것도 없으므로 보상 미실행
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
        }

        @Test
        @DisplayName("delivery cancel 500 → DELIVERY_SERVICE_UNAVAILABLE, hub 재예약 보상 실행")
        void delivery_cancel_500_triggers_hub_re_reserve_compensation() {
            Order order = buildCancellableOrder();
            lenient().when(orderRepository.findOrderById(order.getOrderId()))
                    .thenReturn(Optional.of(order));
            stubHubCancelSuccess();
            stubFor(post(urlEqualTo("/api/v1/internal/deliveries/cancel")).willReturn(serverError()));
            stubHubReserveSuccess(); // 보상: 재고 재예약

            assertThatThrownBy(() -> orderCommandService.cancelOrder(order.getOrderId(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.DELIVERY_SERVICE_UNAVAILABLE));

            // hub 취소 시도됨
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            // delivery 취소 실패 → 보상: hub 재예약
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
        }

        @Test
        @DisplayName("주문 없음 → ORDER_NOT_FOUND, 외부 서비스 호출 없음")
        void order_not_found_no_external_calls() {
            UUID unknownId = UUID.randomUUID();
            lenient().when(orderRepository.findOrderById(unknownId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderCommandService.cancelOrder(unknownId, REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.ORDER_NOT_FOUND));

            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // cancelCompanyOrder
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("cancelCompanyOrder — 서브주문 부분 취소")
    class CancelCompanyOrder {

        @Test
        @DisplayName("정상: hub cancelCompanyStock 호출, 전체 delivery cancel 미호출")
        void success_calls_hub_company_cancel_only() {
            Order order = buildCancellableOrder();
            CompanyOrder co = order.getCompanyOrders().get(0);
            lenient().when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));
            stubHubCompanyStockCancelSuccess();

            orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), REQUESTER_ID);

            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel/company")));
            // 전체 취소(cancelDeliveries)는 cancelOrder 경로 전용 — 미호출
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
        }

        @Test
        @DisplayName("hub cancelCompanyStock 500 → HUB_SERVICE_UNAVAILABLE")
        void hub_company_cancel_500_throws_hub_error() {
            Order order = buildCancellableOrder();
            CompanyOrder co = order.getCompanyOrders().get(0);
            lenient().when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel/company")).willReturn(serverError()));

            assertThatThrownBy(() -> orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.HUB_SERVICE_UNAVAILABLE));
        }

        @Test
        @DisplayName("서브주문 없음 → COMPANY_ORDER_NOT_FOUND, 외부 서비스 호출 없음")
        void company_order_not_found_no_external_calls() {
            UUID unknownId = UUID.randomUUID();
            lenient().when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(unknownId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderCommandService.cancelCompanyOrder(unknownId, REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));

            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel/company")));
        }
    }
}
