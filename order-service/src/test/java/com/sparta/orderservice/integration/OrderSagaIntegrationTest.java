package com.sparta.orderservice.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.global.security.AuthContext;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.application.service.OrderCommandService;
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
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

/**
 * Saga 보상 트랜잭션 로컬 통합 테스트
 *
 * WireMock으로 hub/company/delivery 외부 서비스를 모킹하여
 * 실제 Feign HTTP 호출이 올바른 순서로 실행되고,
 * 실패 시 보상 트랜잭션(cancelStock, cancelDeliveries)이 정상 발동되는지 검증합니다.
 *ㄱ
 * - @MockBean 레포지토리: 실제 DB(PostgreSQL) 없이 실행 가능
 * - @AutoConfigureWireMock: 랜덤 포트 WireMock, wiremock.server.port 프로퍼티로 주입
 * - Feign URL: application-integration-test.yml에서 WireMock 포트로 오버라이드
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = IntegrationTestApplication.class)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("integration-test")
@DisplayName("OrderCommandService Saga 통합 테스트")
class OrderSagaIntegrationTest {

    // DB 연결 없이 실행하기 위해 도메인 레포지토리 전체를 목(Mock)으로 대체
    @MockBean private OrderRepository orderRepository;
    @MockBean private CompanyOrderRepository companyOrderRepository;
    @MockBean private PaymentRepository paymentRepository;
    @MockBean private DraftRepository draftRepository;
    @MockBean private AuthContext authContext;

    @Autowired private OrderCommandService orderCommandService;

    // 테스트 고정 UUID — 재현성을 위해 static 선언
    private static final UUID RECEIVER_COMPANY_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
    private static final UUID SUPPLIER_COMPANY_ID = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000002");
    private static final UUID RECEIVER_HUB_ID     = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000001");
    private static final UUID SUPPLIER_HUB_ID     = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
    private static final UUID PRODUCT_OPTION_ID   = UUID.fromString("cccccccc-0000-0000-0000-000000000001");
    private static final UUID REQUESTER_ID        = UUID.fromString("dddddddd-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        WireMock.reset();
        lenient().when(authContext.isCompanyManager()).thenReturn(true);
        // 레포지토리 저장: 전달받은 엔티티를 그대로 반환 (실제 DB persist 시뮬레이션)
        lenient().when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // WireMock Stub 헬퍼
    // ──────────────────────────────────────────────────────────────────────────

    private void stubCompanyHubMappingSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/companies/hub-mapping"))
                .willReturn(okJson("""
                        {
                          "mappings": {
                            "%s": {"companyId": "%s", "hubId": "%s", "companyName": "수령업체"},
                            "%s": {"companyId": "%s", "hubId": "%s", "companyName": "공급업체"}
                          }
                        }
                        """.formatted(
                        RECEIVER_COMPANY_ID, RECEIVER_COMPANY_ID, RECEIVER_HUB_ID,
                        SUPPLIER_COMPANY_ID, SUPPLIER_COMPANY_ID, SUPPLIER_HUB_ID))));
    }

    private void stubHubReserveSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/inventory/reserve"))
                .willReturn(ok()));
    }

    private void stubHubCancelSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel"))
                .willReturn(ok()));
    }

    private void stubDeliveryCreateSuccess() {
        // companyOrderId는 도메인 내부에서 생성되므로 고정할 수 없음
        // → 응답의 companyOrderId가 실제와 다르더라도 Saga 흐름 자체 검증에는 지장 없음
        stubFor(post(urlEqualTo("/api/v1/internal/deliveries"))
                .willReturn(okJson("""
                        [{"deliveryId": "%s", "companyOrderId": "%s", "status": "WAITING"}]
                        """.formatted(UUID.randomUUID(), SUPPLIER_COMPANY_ID))));
    }

    private void stubDeliveryCancelSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/deliveries/cancel"))
                .willReturn(ok()));
    }

    private CreateOrderCommand buildCommand() {
        return new CreateOrderCommand(
                RECEIVER_COMPANY_ID,
                "홍길동", "010-1234-5678", null,
                "{\"address\":\"서울시 강남구\"}",
                LocalDateTime.now().plusDays(7), null,
                List.of(new CreateOrderCommand.CompanyOrderCommand(
                        SUPPLIER_COMPANY_ID,
                        List.of(new CreateOrderCommand.OrderItemCommand(
                                PRODUCT_OPTION_ID, 2, BigDecimal.valueOf(10000)
                        ))
                ))
        );
    }

    // ──────────────────────────────────────────────────────────────────────────
    // 정상 흐름
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("허브 매핑 → 재고 예약 → 배송 생성 → DB 저장 순으로 HTTP 호출 확인")
        void all_external_calls_in_order_and_no_compensation() {
            stubCompanyHubMappingSuccess();
            stubHubReserveSuccess();
            stubDeliveryCreateSuccess();

            OrderResult result = orderCommandService.createOrder(buildCommand(), REQUESTER_ID);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo("PENDING");
            assertThat(result.totalPrice()).isEqualByComparingTo(BigDecimal.valueOf(20000));

            // 정방향 HTTP 호출 확인
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/companies/hub-mapping")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
            // 보상 HTTP 호출 없음
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));

            org.mockito.Mockito.verify(orderRepository).save(any());
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Saga 보상 트랜잭션
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Saga 보상 트랜잭션")
    class SagaCompensation {

        @Test
        @DisplayName("배송 생성 500 → cancelStock 보상 실행, cancelDeliveries 미호출, DELIVERY_SERVICE_UNAVAILABLE")
        void delivery_fails_triggers_cancel_stock_not_cancel_delivery() {
            stubCompanyHubMappingSuccess();
            stubHubReserveSuccess();
            stubHubCancelSuccess();
            stubFor(post(urlEqualTo("/api/v1/internal/deliveries"))
                    .willReturn(serverError()));

            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.DELIVERY_SERVICE_UNAVAILABLE));

            // 재고 예약은 호출됨
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
            // 배송 생성 실패 후 재고 보상 실행
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            // 배송이 성공한 적 없으므로 배송 취소 보상 미호출
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));

            org.mockito.Mockito.verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("hub 재고 예약 409 → STOCK_INSUFFICIENT, cancelStock 보상 실행, 배송 생성 미호출")
        void hub_reserve_409_triggers_cancel_stock_compensation() {
            stubCompanyHubMappingSuccess();
            stubHubCancelSuccess();
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/reserve"))
                    .willReturn(aResponse().withStatus(409)));

            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.STOCK_INSUFFICIENT));

            // 보상: cancelStock 호출
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            // 재고 예약 실패 → 배송 생성은 미호출
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
        }

        @Test
        @DisplayName("hub 재고 예약 500 → HUB_SERVICE_UNAVAILABLE, cancelStock 보상 실행")
        void hub_reserve_500_triggers_cancel_stock_compensation() {
            stubCompanyHubMappingSuccess();
            stubHubCancelSuccess();
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/reserve"))
                    .willReturn(serverError()));

            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.HUB_SERVICE_UNAVAILABLE));

            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
        }

        @Test
        @DisplayName("DB 저장 실패 → cancelDeliveries + cancelStock 보상 LIFO 순으로 실행")
        void db_save_fails_triggers_delivery_then_stock_compensation() {
            stubCompanyHubMappingSuccess();
            stubHubReserveSuccess();
            stubDeliveryCreateSuccess();
            stubHubCancelSuccess();
            stubDeliveryCancelSuccess();
            // 저장 실패 시뮬레이션
            org.mockito.Mockito.when(orderRepository.save(any()))
                    .thenThrow(new RuntimeException("DB connection lost"));

            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB connection lost");

            // 두 보상 모두 실행됨 (LIFO: cancelDeliveries → cancelStock)
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
        }

        @Test
        @DisplayName("company 404 → COMPANY_NOT_FOUND, 재고 예약·취소 미호출")
        void company_not_found_no_stock_operations() {
            stubFor(post(urlEqualTo("/api/v1/internal/companies/hub-mapping"))
                    .willReturn(notFound()));

            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.COMPANY_NOT_FOUND));

            // 허브 매핑 실패 → Saga try 블록 진입 전 예외 → 보상 없음
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
        }

        @Test
        @DisplayName("cancelStock 보상 자체 실패 시 나머지 보상은 계속 실행 (executeCompensations 내성)")
        void compensation_failure_does_not_stop_remaining_compensations() {
            stubCompanyHubMappingSuccess();
            stubHubReserveSuccess();
            stubDeliveryCreateSuccess();
            // cancelDeliveries 보상은 성공
            stubDeliveryCancelSuccess();
            // cancelStock 보상은 실패 (500) → 로그 후 무시, 예외 전파 안 됨
            stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel"))
                    .willReturn(serverError()));
            org.mockito.Mockito.when(orderRepository.save(any()))
                    .thenThrow(new RuntimeException("DB error"));

            // 보상 중 cancelStock이 실패해도 원래 예외("DB error")가 전파됨
            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("DB error");

            // cancelDeliveries와 cancelStock 모두 시도됨
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // FeignErrorDecoder 에러 코드 매핑 검증
    // ──────────────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("FeignErrorDecoder 에러 코드 매핑")
    class FeignErrorMapping {

        @Test
        @DisplayName("company 500 → COMPANY_SERVICE_UNAVAILABLE")
        void company_500_mapped_to_company_service_unavailable() {
            stubFor(post(urlEqualTo("/api/v1/internal/companies/hub-mapping"))
                    .willReturn(serverError()));

            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.COMPANY_SERVICE_UNAVAILABLE));
        }

        @Test
        @DisplayName("delivery 4xx → DELIVERY_SERVICE_UNAVAILABLE (모든 오류 동일 매핑)")
        void delivery_4xx_mapped_to_delivery_service_unavailable() {
            stubCompanyHubMappingSuccess();
            stubHubReserveSuccess();
            stubHubCancelSuccess();
            stubFor(post(urlEqualTo("/api/v1/internal/deliveries"))
                    .willReturn(aResponse().withStatus(400)));

            assertThatThrownBy(() -> orderCommandService.createOrder(buildCommand(), REQUESTER_ID))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(OrderErrorCode.DELIVERY_SERVICE_UNAVAILABLE));
        }
    }
}
