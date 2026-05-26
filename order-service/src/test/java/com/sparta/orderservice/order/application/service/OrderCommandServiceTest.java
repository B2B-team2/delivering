package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.port.CompanyPort;
import com.sparta.orderservice.global.security.SecurityUtils;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.application.port.DeliveryPort;
import com.sparta.orderservice.order.application.port.HubStockPort;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.event.OrderCancelledEvent;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderCommandService 단위 테스트")
class OrderCommandServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CompanyOrderRepository companyOrderRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private HubStockPort hubStockPort;
    @Mock
    private CompanyPort companyPort;
    @Mock
    private DeliveryPort deliveryPort;
    @Mock
    private OrderWriter orderWriter;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private OrderCommandService orderCommandService;

    private UUID receiverCompanyId;
    private UUID supplierCompanyId;
    private UUID requesterId;
    private Map<UUID, UUID> hubIdMap;
    private CreateOrderCommand command;

    @BeforeEach
    void setUp() {
        lenient().when(securityUtils.isMaster()).thenReturn(true);

        receiverCompanyId = UUID.randomUUID();
        supplierCompanyId = UUID.randomUUID();
        requesterId = UUID.randomUUID();

        hubIdMap = new HashMap<>();
        hubIdMap.put(receiverCompanyId, UUID.randomUUID());
        hubIdMap.put(supplierCompanyId, UUID.randomUUID());

        command = new CreateOrderCommand(
                receiverCompanyId, "홍길동", "010-1234-5678", null,
                "{\"address\":\"서울시\"}",
                LocalDateTime.now().plusDays(7), null,
                List.of(new CreateOrderCommand.CompanyOrderCommand(
                        supplierCompanyId,
                        List.of(new CreateOrderCommand.OrderItemCommand(
                                UUID.randomUUID(), 2, BigDecimal.valueOf(10000)
                        ))
                ))
        );
    }

    // --- createOrder ---

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("정상 흐름: hub 조회 → 재고 예약 → 배송 생성 → 저장 순으로 실행")
        void success_executes_all_steps_in_order() {
            when(companyPort.getHubIds(anyList())).thenReturn(hubIdMap);
            when(deliveryPort.createDeliveries(any(), any(), any())).thenReturn(Collections.emptyMap());
            when(orderWriter.saveOrderWithEvent(any())).thenAnswer(inv -> OrderResult.from((Order) inv.getArgument(0)));

            OrderResult result = orderCommandService.createOrder(command, requesterId);

            verify(companyPort).getHubIds(anyList());
            verify(hubStockPort).reserveStock(any());
            verify(deliveryPort).createDeliveries(any(), any(), any());
            verify(orderWriter).saveOrderWithEvent(any());

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(OrderStatus.PENDING.name());
        }

        @Test
        @DisplayName("재고 예약 실패 → cancelStock 보상 실행 후 예외 전파")
        void reserve_stock_fails_triggers_cancel_stock_compensation() {
            when(companyPort.getHubIds(anyList())).thenReturn(hubIdMap);
            doThrow(new RuntimeException("hub down")).when(hubStockPort).reserveStock(any());

            assertThatThrownBy(() -> orderCommandService.createOrder(command, requesterId))
                    .isInstanceOf(RuntimeException.class);

            verify(hubStockPort).cancelStock(any());
            verify(deliveryPort, never()).cancelDeliveries(any());
            verify(orderWriter, never()).saveOrderWithEvent(any());
        }

        @Test
        @DisplayName("배송 생성 실패 → cancelStock 보상 실행, cancelDeliveries는 미실행")
        void create_deliveries_fails_triggers_cancel_stock_only() {
            when(companyPort.getHubIds(anyList())).thenReturn(hubIdMap);
            doNothing().when(hubStockPort).reserveStock(any());
            doThrow(new RuntimeException("delivery down")).when(deliveryPort).createDeliveries(any(), any(), any());

            assertThatThrownBy(() -> orderCommandService.createOrder(command, requesterId))
                    .isInstanceOf(RuntimeException.class);

            verify(hubStockPort).cancelStock(any());
            verify(deliveryPort, never()).cancelDeliveries(any());
        }

        @Test
        @DisplayName("DB 저장 실패 → cancelDeliveries + cancelStock 역순 보상 실행")
        void save_fails_triggers_both_compensations() {
            when(companyPort.getHubIds(anyList())).thenReturn(hubIdMap);
            doNothing().when(hubStockPort).reserveStock(any());
            when(deliveryPort.createDeliveries(any(), any(), any())).thenReturn(Collections.emptyMap());
            doThrow(new RuntimeException("db error")).when(orderWriter).saveOrderWithEvent(any());

            assertThatThrownBy(() -> orderCommandService.createOrder(command, requesterId))
                    .isInstanceOf(RuntimeException.class);

            verify(deliveryPort).cancelDeliveries(any());
            verify(hubStockPort).cancelStock(any());
        }

        @Test
        @DisplayName("hubId 매핑 누락 시 예외 발생 (외부 호출 전 검증)")
        void missing_hub_mapping_throws_before_external_calls() {
            Map<UUID, UUID> incompleteMap = new HashMap<>();
            incompleteMap.put(receiverCompanyId, UUID.randomUUID());
            // supplierCompanyId 매핑 누락
            when(companyPort.getHubIds(anyList())).thenReturn(incompleteMap);

            assertThatThrownBy(() -> orderCommandService.createOrder(command, requesterId))
                    .isInstanceOf(BusinessException.class);

            verify(hubStockPort, never()).reserveStock(any());
        }

        @Test
        @DisplayName("보상 실행 중 예외 발생해도 나머지 보상은 계속 실행됨 (executeCompensations 내성)")
        void compensation_failure_does_not_stop_remaining_compensations() {
            when(companyPort.getHubIds(anyList())).thenReturn(hubIdMap);
            doNothing().when(hubStockPort).reserveStock(any());
            when(deliveryPort.createDeliveries(any(), any(), any())).thenReturn(Collections.emptyMap());
            doThrow(new RuntimeException("db error")).when(orderWriter).saveOrderWithEvent(any());
            // 첫 번째 보상(cancelDeliveries)이 실패해도 두 번째 보상(cancelStock)은 실행
            doThrow(new RuntimeException("delivery cancel failed")).when(deliveryPort).cancelDeliveries(any());

            assertThatThrownBy(() -> orderCommandService.createOrder(command, requesterId))
                    .isInstanceOf(RuntimeException.class);

            // 두 보상 모두 호출됐는지 확인 (첫 번째가 실패해도 두 번째 실행)
            verify(deliveryPort).cancelDeliveries(any());
            verify(hubStockPort).cancelStock(any());
        }
    }

    // --- cancelOrder ---

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        private Order pendingOrder;

        @BeforeEach
        void setUpOrder() {
            pendingOrder = buildPendingOrder();
        }

        @Test
        @DisplayName("정상 취소: 재고 취소 + OrderCancelledEvent 발행")
        void success_cancels_stock_and_publishes_event() {
            UUID orderId = pendingOrder.getOrderId();
            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(pendingOrder));

            orderCommandService.cancelOrder(orderId, requesterId);

            verify(hubStockPort).cancelStock(orderId);
            ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("이미 CANCELLED인 주문 → 예외 발생")
        void already_cancelled_order_throws() {
            pendingOrder.cancel(requesterId);
            UUID orderId = pendingOrder.getOrderId();
            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> orderCommandService.cancelOrder(orderId, requesterId))
                    .isInstanceOf(BusinessException.class);

            verify(hubStockPort, never()).cancelStock(any());
        }

        @Test
        @DisplayName("DELIVERING 상태 주문(취소 불가) → 예외 발생")
        void delivering_order_not_cancellable_throws() {
            pendingOrder.startDelivery();
            UUID orderId = pendingOrder.getOrderId();
            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(pendingOrder));

            assertThatThrownBy(() -> orderCommandService.cancelOrder(orderId, requesterId))
                    .isInstanceOf(BusinessException.class);

            verify(hubStockPort, never()).cancelStock(any());
        }

        @Test
        @DisplayName("재고 취소 성공 후 이벤트 발행 실패 → 재고 재예약 보상 실행")
        void event_publish_fails_triggers_reserve_stock_compensation() {
            UUID orderId = pendingOrder.getOrderId();
            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(pendingOrder));
            doNothing().when(hubStockPort).cancelStock(any());
            doThrow(new RuntimeException("event error")).when(eventPublisher).publishEvent(any());

            assertThatThrownBy(() -> orderCommandService.cancelOrder(orderId, requesterId))
                    .isInstanceOf(RuntimeException.class);

            verify(hubStockPort).reserveStock(any());
        }

        @Test
        @DisplayName("재고 취소 자체 실패 → 보상 스택 미등록 상태이므로 보상 없이 예외 전파")
        void cancel_stock_fails_propagates_without_compensation() {
            UUID orderId = pendingOrder.getOrderId();
            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.of(pendingOrder));
            doThrow(new RuntimeException("hub down")).when(hubStockPort).cancelStock(any());

            assertThatThrownBy(() -> orderCommandService.cancelOrder(orderId, requesterId))
                    .isInstanceOf(RuntimeException.class);

            // cancelStock 실패 전 보상이 등록되지 않았으므로 reserveStock 미호출
            verify(hubStockPort, never()).reserveStock(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("주문 없음 → 예외 발생")
        void order_not_found_throws() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findOrderById(orderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderCommandService.cancelOrder(orderId, requesterId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    // --- cancelCompanyOrder ---

    @Nested
    @DisplayName("cancelCompanyOrder()")
    class CancelCompanyOrder {

        @Test
        @DisplayName("마지막 서브주문 취소 → Order CANCELLED + OrderCancelledEvent 발행")
        void last_company_order_cancelled_publishes_event() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            order.addCompanyOrder(co);
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));

            orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), requesterId);

            verify(hubStockPort).cancelCompanyStock(co.getCompanyOrderId());
            ArgumentCaptor<OrderCancelledEvent> captor = ArgumentCaptor.forClass(OrderCancelledEvent.class);
            verify(eventPublisher).publishEvent(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(order.getOrderId());
        }

        @Test
        @DisplayName("활성 서브주문 잔존 시 Order 상태 변경 없음, 이벤트 미발행")
        void active_sibling_exists_no_event_published() {
            Order order = buildPendingOrder();
            CompanyOrder co1 = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(10000), BigDecimal.ZERO);
            CompanyOrder co2 = CompanyOrder.of(order, UUID.randomUUID(), BigDecimal.valueOf(10000), BigDecimal.ZERO);
            order.addCompanyOrder(co1);
            order.addCompanyOrder(co2);
            co2.prepare();
            co2.ship(); // co2 = SHIPPED (활성) → Order 여전히 진행 중
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co1.getCompanyOrderId()))
                    .thenReturn(Optional.of(co1));

            orderCommandService.cancelCompanyOrder(co1.getCompanyOrderId(), requesterId);

            verify(hubStockPort).cancelCompanyStock(co1.getCompanyOrderId());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("이미 CANCELLED인 서브주문 → 예외 발생")
        void already_cancelled_throws() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            co.cancel(requesterId);
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));

            assertThatThrownBy(() -> orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), requesterId))
                    .isInstanceOf(BusinessException.class);

            verify(hubStockPort, never()).cancelCompanyStock(any());
        }

        @Test
        @DisplayName("SHIPPED 상태 서브주문 → 예외 발생")
        void shipped_status_throws() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            co.prepare();
            co.ship();
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));

            assertThatThrownBy(() -> orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), requesterId))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("cancelCompanyStock 실패 → 보상 미등록이므로 보상 없이 예외 전파")
        void cancel_company_stock_fails_no_compensation() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            order.addCompanyOrder(co);
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));
            doThrow(new RuntimeException("hub down")).when(hubStockPort).cancelCompanyStock(any());

            assertThatThrownBy(() -> orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), requesterId))
                    .isInstanceOf(RuntimeException.class);

            verify(hubStockPort, never()).reserveCompanyStock(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("이벤트 발행 실패(마지막 서브주문) → reserveCompanyStock 보상 실행")
        void event_publish_fails_triggers_reserve_company_stock_compensation() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            order.addCompanyOrder(co);
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));
            doNothing().when(hubStockPort).cancelCompanyStock(any());
            doThrow(new RuntimeException("event error")).when(eventPublisher).publishEvent(any());

            assertThatThrownBy(() -> orderCommandService.cancelCompanyOrder(co.getCompanyOrderId(), requesterId))
                    .isInstanceOf(RuntimeException.class);

            verify(hubStockPort).reserveCompanyStock(co);
        }
    }

    // --- helper ---

    private Order buildPendingOrder() {
        return Order.of(
                receiverCompanyId, "홍길동", "010-1234-5678", null,
                "{\"address\":\"서울시\"}",
                LocalDateTime.now().plusDays(7), null,
                BigDecimal.valueOf(20000), BigDecimal.ZERO, BigDecimal.valueOf(20000)
        );
    }
}
