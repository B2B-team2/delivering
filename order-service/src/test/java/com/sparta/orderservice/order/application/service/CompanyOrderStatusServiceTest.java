package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.CompanyOrderDeliveredResult;
import com.sparta.orderservice.order.application.dto.CompanyOrderResult;
import com.sparta.orderservice.order.application.port.HubStockPort;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CompanyOrderStatusService 단위 테스트")
class CompanyOrderStatusServiceTest {

    @Mock
    private CompanyOrderRepository companyOrderRepository;
    @Mock
    private HubStockPort hubStockPort;
    @Mock
    private CompanyOrderWriter companyOrderWriter;

    @InjectMocks
    private CompanyOrderStatusService companyOrderStatusService;

    private UUID supplierCompanyId;
    private UUID receiverCompanyId;

    @BeforeEach
    void setUp() {
        supplierCompanyId = UUID.randomUUID();
        receiverCompanyId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("prepareCompanyOrder()")
    class PrepareCompanyOrder {

        @Test
        @DisplayName("ORDERED → PREPARING 상태 전환 성공")
        void ordered_to_preparing_success() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            when(companyOrderRepository.findCompanyOrderWithItemsAndOrder(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));

            CompanyOrderResult result = companyOrderStatusService.prepareCompanyOrder(co.getCompanyOrderId());

            assertThat(result.status()).isEqualTo(CompanyOrderStatus.PREPARING.name());
        }

        @Test
        @DisplayName("ORDERED 아닌 상태(PREPARING)에서 호출 → 예외 발생")
        void non_ordered_status_throws() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            co.prepare();
            when(companyOrderRepository.findCompanyOrderWithItemsAndOrder(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));

            assertThatThrownBy(() -> companyOrderStatusService.prepareCompanyOrder(co.getCompanyOrderId()))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("shipCompanyOrder()")
    class ShipCompanyOrder {

        @Test
        @DisplayName("PREPARING → SHIPPED, 재고 차감 호출, 첫 출고 시 Order → DELIVERING")
        void preparing_to_shipped_and_order_starts_delivery() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            order.addCompanyOrder(co);
            // persistShip이 커밋 완료된 상태(SHIPPED + DELIVERING)의 CompanyOrder를 반환한다고 모킹
            co.prepare();
            co.ship();
            order.startDelivery();
            when(companyOrderWriter.persistShip(co.getCompanyOrderId())).thenReturn(co);

            CompanyOrderResult result = companyOrderStatusService.shipCompanyOrder(co.getCompanyOrderId());

            assertThat(result.status()).isEqualTo(CompanyOrderStatus.SHIPPED.name());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERING);
            verify(hubStockPort).deductStock(co);
        }

        @Test
        @DisplayName("PREPARING 아닌 상태(ORDERED) → persistShip에서 예외 발생, 재고 차감 미호출")
        void non_preparing_status_throws() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            when(companyOrderWriter.persistShip(co.getCompanyOrderId()))
                    .thenThrow(new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION));

            assertThatThrownBy(() -> companyOrderStatusService.shipCompanyOrder(co.getCompanyOrderId()))
                    .isInstanceOf(BusinessException.class);

            verify(hubStockPort, never()).deductStock(any());
        }

        @Test
        @DisplayName("재고 차감 실패 → revertShip 보상 실행 후 예외 전파")
        void deduct_stock_fails_triggers_revert_ship_compensation() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            order.addCompanyOrder(co);
            co.prepare();
            co.ship();
            order.startDelivery();
            when(companyOrderWriter.persistShip(co.getCompanyOrderId())).thenReturn(co);
            doThrow(new RuntimeException("hub down")).when(hubStockPort).deductStock(co);

            assertThatThrownBy(() -> companyOrderStatusService.shipCompanyOrder(co.getCompanyOrderId()))
                    .isInstanceOf(RuntimeException.class);

            verify(companyOrderWriter).revertShip(co.getCompanyOrderId());
        }
    }

    @Nested
    @DisplayName("confirmDelivery()")
    class ConfirmDelivery {

        @Test
        @DisplayName("SHIPPED → DELIVERED, 마지막 CompanyOrder이면 Order → COMPLETED")
        void shipped_to_delivered_and_order_completes() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            order.addCompanyOrder(co);
            co.prepare();
            co.ship();
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));

            CompanyOrderDeliveredResult result = companyOrderStatusService.confirmDelivery(co.getCompanyOrderId(), UUID.randomUUID());

            assertThat(result.companyOrderStatus()).isEqualTo(CompanyOrderStatus.DELIVERED.name());
            assertThat(result.orderStatus()).isEqualTo(OrderStatus.COMPLETED.name());
        }

        @Test
        @DisplayName("SHIPPED 아닌 상태(PREPARING)에서 호출 → 예외 발생")
        void non_shipped_status_throws() {
            Order order = buildPendingOrder();
            CompanyOrder co = CompanyOrder.of(order, supplierCompanyId, BigDecimal.valueOf(20000), BigDecimal.ZERO);
            co.prepare();
            when(companyOrderRepository.findCompanyOrderWithOrderAndSiblings(co.getCompanyOrderId()))
                    .thenReturn(Optional.of(co));

            assertThatThrownBy(() -> companyOrderStatusService.confirmDelivery(co.getCompanyOrderId(), UUID.randomUUID()))
                    .isInstanceOf(BusinessException.class);
        }
    }

    private Order buildPendingOrder() {
        return Order.of(
                receiverCompanyId, "홍길동", "010-1234-5678", null,
                "{\"address\":\"서울시\"}",
                LocalDateTime.now().plusDays(7), null,
                BigDecimal.valueOf(20000), BigDecimal.ZERO, BigDecimal.valueOf(20000)
        );
    }
}
