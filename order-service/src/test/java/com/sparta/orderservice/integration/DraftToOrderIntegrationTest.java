package com.sparta.orderservice.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.application.service.DraftService;
import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import com.sparta.orderservice.global.security.AuthContext;
import com.sparta.orderservice.order.application.dto.OrderResult;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;

/**
 * DraftService.createOrderFromDraft 통합 테스트
 *
 * 커버리지:
 * - ProductClient.getProductOptionInfos  (product-service)
 * - CompanyClient.getDefaultDeliveryAddress (company-service, 배송지 미입력 시)
 * - 이후 createOrder Saga (hub-service, delivery-service) 전체 체인
 * - draft 삭제 실패 시 cancelOrder 보상 실행 검증
 *
 * WireMock으로 4개 외부 서비스(product/company/hub/delivery)를 모킹합니다.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = IntegrationTestApplication.class)
@AutoConfigureWireMock(port = 0)
@ActiveProfiles("integration-test")
@DisplayName("DraftService.createOrderFromDraft 통합 테스트")
class DraftToOrderIntegrationTest {

    @MockBean private OrderRepository orderRepository;
    @MockBean private CompanyOrderRepository companyOrderRepository;
    @MockBean private PaymentRepository paymentRepository;
    @MockBean private DraftRepository draftRepository;
    @MockBean private AuthContext authContext;

    @Autowired private DraftService draftService;

    // 고정 UUID
    private static final UUID USER_ID             = UUID.fromString("ffff0001-0000-0000-0000-000000000001");
    private static final UUID RECEIVER_COMPANY_ID = UUID.fromString("aaaa0001-0000-0000-0000-000000000001");
    private static final UUID SUPPLIER_COMPANY_ID = UUID.fromString("aaaa0001-0000-0000-0000-000000000002");
    private static final UUID RECEIVER_HUB_ID     = UUID.fromString("bbbb0001-0000-0000-0000-000000000001");
    private static final UUID SUPPLIER_HUB_ID     = UUID.fromString("bbbb0001-0000-0000-0000-000000000002");
    private static final UUID PRODUCT_ID          = UUID.fromString("cccc0001-0000-0000-0000-000000000001");
    private static final UUID PRODUCT_OPTION_ID   = UUID.fromString("cccc0001-0000-0000-0000-000000000002");
    private static final UUID DRAFT_ID            = UUID.fromString("eeee0001-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        WireMock.reset();
        lenient().when(authContext.isCompanyManager()).thenReturn(true);
        lenient().when(authContext.isMaster()).thenReturn(false);
        lenient().when(authContext.getCompanyId()).thenReturn(RECEIVER_COMPANY_ID);
        // 결제 생성 이벤트: paymentRepository.save() 호출을 그대로 반환
        lenient().when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        // 주문 저장 이벤트: orderRepository.save() 호출을 그대로 반환
        lenient().when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // ── 도메인 객체 생성 헬퍼 ───────────────────────────────────────────

    private Draft buildDraft() {
        return Draft.of(USER_ID, PRODUCT_ID, PRODUCT_OPTION_ID, 2);
    }

    // ── WireMock stub 헬퍼 ──────────────────────────────────────────────

    /** product-service: 상품 옵션 일괄 조회 */
    private void stubProductOptionsSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/product-options/details"))
                .willReturn(okJson("""
                        {
                          "optionsMap": {
                            "%s": {
                              "productOptionId": "%s",
                              "companyId": "%s",
                              "unitPrice": 10000
                            }
                          }
                        }
                        """.formatted(PRODUCT_OPTION_ID, PRODUCT_OPTION_ID, SUPPLIER_COMPANY_ID))));
    }

    /** company-service: 허브 매핑 조회 */
    private void stubHubMappingSuccess() {
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

    /** company-service: 기본 배송지 조회 (배송지 미입력 시 자동 조회) */
    private void stubDefaultDeliveryAddressSuccess() {
        stubFor(get(urlPathMatching("/api/v1/internal/companies/.*/default-address"))
                .willReturn(okJson("""
                        {
                          "address": "{\\"address\\":\\"서울시 강남구\\"}",
                          "addressDetail": "101호",
                          "recipientName": "홍길동",
                          "phone": "010-1234-5678"
                        }
                        """)));
    }

    /** hub-service: 재고 예약 */
    private void stubHubReserveSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/inventory/reserve")).willReturn(ok()));
    }

    private void stubHubCancelSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/inventory/cancel")).willReturn(ok()));
    }

    /** delivery-service: 배송 생성 */
    private void stubDeliveryCreateSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/deliveries"))
                .willReturn(okJson("""
                        [{"deliveryId": "%s", "companyOrderId": "%s", "status": "WAITING"}]
                        """.formatted(UUID.randomUUID(), SUPPLIER_COMPANY_ID))));
    }

    private void stubDeliveryCancelSuccess() {
        stubFor(post(urlEqualTo("/api/v1/internal/deliveries/cancel")).willReturn(ok()));
    }

    /** draft 삭제 (DraftWriter.deleteAll) — findAllDraftsByIds 두 번째 호출 반환 */
    private void stubDraftRepositoryForDelete() {
        Draft draft = buildDraft();
        // readAndValidateDrafts(첫 번째 호출) + deleteAll(두 번째 호출) 모두 대응
        lenient().when(draftRepository.findAllDraftsByIds(List.of(DRAFT_ID)))
                .thenReturn(List.of(draft));
    }

    /** 명령 객체 생성: address=null → company-service 기본 배송지 자동 조회 경로 */
    private CreateOrderFromDraftCommand buildCommandWithoutAddress() {
        return new CreateOrderFromDraftCommand(
                USER_ID,
                RECEIVER_COMPANY_ID,
                List.of(DRAFT_ID),
                null,          // address null → 기본 배송지 자동 조회
                null,          // recipientName
                null,          // phone
                null,          // slackId
                LocalDateTime.now().plusDays(7),
                null           // requestMemo
        );
    }

    /** 명령 객체 생성: address 직접 입력 → company-service 기본 배송지 조회 생략 */
    private CreateOrderFromDraftCommand buildCommandWithAddress() {
        return new CreateOrderFromDraftCommand(
                USER_ID,
                RECEIVER_COMPANY_ID,
                List.of(DRAFT_ID),
                "{\"address\":\"서울시 강남구\"}",
                "홍길동",
                "010-0000-0000",
                null,          // slackId
                LocalDateTime.now().plusDays(7),
                null           // requestMemo
        );
    }

    // ══════════════════════════════════════════════════════════════════════
    // 정상 흐름
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("정상 흐름")
    class Success {

        @Test
        @DisplayName("배송지 미입력: product → defaultAddress → hubMapping → reserve → delivery 순으로 호출")
        void without_address_calls_all_external_services_in_order() {
            Draft draft = buildDraft();
            lenient().when(draftRepository.findAllDraftsByIds(List.of(DRAFT_ID)))
                    .thenReturn(List.of(draft));

            stubProductOptionsSuccess();
            stubDefaultDeliveryAddressSuccess();
            stubHubMappingSuccess();
            stubHubReserveSuccess();
            stubDeliveryCreateSuccess();

            OrderResult result = draftService.createOrderFromDraft(buildCommandWithoutAddress());

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo("PENDING");

            // ProductClient 호출 확인
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/product-options/details")));
            // CompanyClient 기본 배송지 조회 확인
            verify(1, getRequestedFor(urlPathMatching("/api/v1/internal/companies/.*/default-address")));
            // createOrder Saga 확인
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/companies/hub-mapping")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
            // 보상 호출 없음
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
        }

        @Test
        @DisplayName("배송지 직접 입력: company-service 기본 배송지 조회 생략")
        void with_address_skips_default_address_call() {
            Draft draft = buildDraft();
            lenient().when(draftRepository.findAllDraftsByIds(List.of(DRAFT_ID)))
                    .thenReturn(List.of(draft));

            stubProductOptionsSuccess();
            stubHubMappingSuccess();
            stubHubReserveSuccess();
            stubDeliveryCreateSuccess();

            draftService.createOrderFromDraft(buildCommandWithAddress());

            // 배송지 직접 입력 → getDefaultDeliveryAddress 미호출
            verify(0, getRequestedFor(urlPathMatching("/api/v1/internal/companies/.*/default-address")));
            // 나머지 호출 정상
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/product-options/details")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ProductClient 오류
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ProductClient 오류")
    class ProductClientError {

        @Test
        @DisplayName("product-service 500 → 예외 전파, 이후 외부 호출 없음")
        void product_500_throws_and_no_further_calls() {
            Draft draft = buildDraft();
            lenient().when(draftRepository.findAllDraftsByIds(List.of(DRAFT_ID)))
                    .thenReturn(List.of(draft));
            stubFor(post(urlEqualTo("/api/v1/internal/product-options/details"))
                    .willReturn(serverError()));

            assertThatThrownBy(() -> draftService.createOrderFromDraft(buildCommandWithoutAddress()))
                    .isInstanceOf(BusinessException.class);

            // ProductClient 실패 → 이후 모든 외부 호출 없음
            verify(0, getRequestedFor(urlPathMatching("/api/v1/internal/companies/.*/default-address")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/companies/hub-mapping")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
        }

        @Test
        @DisplayName("draft 없음(빈 목록 반환) → DRAFT_NOT_FOUND, 외부 호출 없음")
        void draft_not_found_no_external_calls() {
            lenient().when(draftRepository.findAllDraftsByIds(List.of(DRAFT_ID)))
                    .thenReturn(List.of()); // 빈 목록 → DRAFT_NOT_FOUND

            assertThatThrownBy(() -> draftService.createOrderFromDraft(buildCommandWithoutAddress()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(DraftErrorCode.DRAFT_NOT_FOUND));

            verify(0, postRequestedFor(urlEqualTo("/api/v1/internal/product-options/details")));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Draft 삭제 실패 → 주문 취소 보상
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Draft 삭제 실패 시 cancelOrder 보상")
    class DraftDeleteCompensation {

        /**
         * cancelOrder 보상 실행에 필요한 Order 준비
         * - DraftService catch 블록에서 cancelOrder(orderId) 호출 시
         *   orderRepository.findOrderById(orderId)가 필요하므로 any() 매핑으로 반환
         * - receiverCompanyId를 authContext.getCompanyId()와 일치시켜 FORBIDDEN 방지
         */
        private Order buildCancellableOrderForCompensation() {
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

        @Test
        @DisplayName("draft 삭제 실패 → cancelOrder 보상: hub cancel + delivery cancel 호출")
        void draft_delete_fails_triggers_cancel_order_compensation() {
            Draft draft = buildDraft();
            // 1st call (readAndValidateDrafts): 정상 반환
            // 2nd call (deleteAll): 빈 목록 → DRAFT_NOT_FOUND → DraftService catch 블록에서 cancelOrder 보상 실행
            org.mockito.Mockito.when(draftRepository.findAllDraftsByIds(List.of(DRAFT_ID)))
                    .thenReturn(List.of(draft))
                    .thenReturn(List.of());

            // cancelOrder 보상 실행 시 orderRepository.findOrderById() 필요
            Order compensationOrder = buildCancellableOrderForCompensation();
            lenient().when(orderRepository.findOrderById(any())).thenReturn(Optional.of(compensationOrder));

            stubProductOptionsSuccess();
            stubDefaultDeliveryAddressSuccess();
            stubHubMappingSuccess();
            stubHubReserveSuccess();
            stubDeliveryCreateSuccess();
            stubHubCancelSuccess();
            stubDeliveryCancelSuccess();

            assertThatThrownBy(() -> draftService.createOrderFromDraft(buildCommandWithoutAddress()))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                            .isEqualTo(DraftErrorCode.DRAFT_NOT_FOUND));

            // 주문 생성 정방향 호출 확인
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/reserve")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries")));
            // draft 삭제 실패 → cancelOrder 보상 실행
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/inventory/cancel")));
            verify(1, postRequestedFor(urlEqualTo("/api/v1/internal/deliveries/cancel")));
        }
    }
}
