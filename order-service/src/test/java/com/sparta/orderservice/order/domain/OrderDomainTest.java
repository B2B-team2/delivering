package com.sparta.orderservice.order.domain;

import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Order 도메인 단위 테스트")
class OrderDomainTest {

    private Order order;
    private CompanyOrder companyOrder;

    @BeforeEach
    void setUp() {
        order = buildOrder();
        companyOrder = CompanyOrder.of(order, UUID.randomUUID(), BigDecimal.valueOf(20000), BigDecimal.ZERO);
        order.addCompanyOrder(companyOrder);
    }

    private Order buildOrder() {
        return Order.of(
                UUID.randomUUID(), "홍길동", "010-1234-5678", null,
                "{\"address\":\"서울시\"}",
                LocalDateTime.now().plusDays(7), null,
                BigDecimal.valueOf(20000), BigDecimal.ZERO, BigDecimal.valueOf(20000)
        );
    }

    @Nested
    @DisplayName("UUID 사전 생성 (save() 이전 참조 가능)")
    class UuidPreGeneration {

        @Test
        @DisplayName("Order 생성 직후 orderId가 null이 아님")
        void order_id_is_not_null_before_save() {
            assertThat(buildOrder().getOrderId()).isNotNull();
        }

        @Test
        @DisplayName("CompanyOrder 생성 직후 companyOrderId가 null이 아님")
        void company_order_id_is_not_null_before_save() {
            Order newOrder = buildOrder();
            CompanyOrder co = CompanyOrder.of(newOrder, UUID.randomUUID(), BigDecimal.TEN, BigDecimal.ZERO);
            assertThat(co.getCompanyOrderId()).isNotNull();
        }

        @Test
        @DisplayName("두 Order 인스턴스의 orderId는 서로 다름")
        void each_order_has_unique_id() {
            assertThat(buildOrder().getOrderId()).isNotEqualTo(buildOrder().getOrderId());
        }

        @Test
        @DisplayName("신규 Order: version == null → isNew() = true")
        void new_order_is_new_returns_true() {
            assertThat(buildOrder().isNew()).isTrue();
        }

        @Test
        @DisplayName("신규 CompanyOrder: version == null → isNew() = true")
        void new_company_order_is_new_returns_true() {
            Order newOrder = buildOrder();
            CompanyOrder co = CompanyOrder.of(newOrder, UUID.randomUUID(), BigDecimal.TEN, BigDecimal.ZERO);
            assertThat(co.isNew()).isTrue();
        }
    }

    @Nested
    @DisplayName("Order.isCancellable()")
    class IsCancellable {

        @Test
        @DisplayName("PENDING + 출고/수령 서브주문 없음 → 취소 가능")
        void pending_with_no_shipped_is_cancellable() {
            assertThat(order.isCancellable()).isTrue();
        }

        @Test
        @DisplayName("DELIVERING 상태 → 취소 불가")
        void delivering_status_is_not_cancellable() {
            order.startDelivery();
            assertThat(order.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("COMPLETED 상태 → 취소 불가")
        void completed_status_is_not_cancellable() {
            companyOrder.prepare();
            companyOrder.ship();
            companyOrder.deliver();
            order.updateStatus(null);

            assertThat(order.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("SHIPPED 서브주문 존재 → 취소 불가")
        void has_shipped_company_order_is_not_cancellable() {
            companyOrder.prepare();
            companyOrder.ship();

            assertThat(order.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("DELIVERED 서브주문 존재 → 취소 불가")
        void has_delivered_company_order_is_not_cancellable() {
            companyOrder.prepare();
            companyOrder.ship();
            companyOrder.deliver();

            assertThat(order.isCancellable()).isFalse();
        }

        @Test
        @DisplayName("PREPARING 서브주문만 있으면 → 취소 가능")
        void only_preparing_company_order_is_cancellable() {
            companyOrder.prepare();

            assertThat(order.isCancellable()).isTrue();
        }
    }

    @Nested
    @DisplayName("Order.updateStatus()")
    class UpdateStatus {

        @Test
        @DisplayName("모든 서브주문 DELIVERED → Order COMPLETED")
        void all_delivered_results_in_completed() {
            companyOrder.prepare();
            companyOrder.ship();
            companyOrder.deliver();

            order.updateStatus(null);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("모든 서브주문 CANCELLED → Order CANCELLED")
        void all_cancelled_results_in_cancelled() {
            UUID requesterId = UUID.randomUUID();
            companyOrder.cancel(requesterId);

            order.updateStatus(requesterId);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("DELIVERED + CANCELLED 혼재 → Order COMPLETED")
        void mixed_delivered_and_cancelled_results_in_completed() {
            Order multiOrder = buildOrder();
            CompanyOrder co1 = CompanyOrder.of(multiOrder, UUID.randomUUID(), BigDecimal.valueOf(10000), BigDecimal.ZERO);
            CompanyOrder co2 = CompanyOrder.of(multiOrder, UUID.randomUUID(), BigDecimal.valueOf(10000), BigDecimal.ZERO);
            multiOrder.addCompanyOrder(co1);
            multiOrder.addCompanyOrder(co2);

            co1.prepare();
            co1.ship();
            co1.deliver();
            co2.cancel(UUID.randomUUID());

            multiOrder.updateStatus(null);

            assertThat(multiOrder.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("활성 서브주문 존재 → Order 상태 변경 없음")
        void has_active_company_order_keeps_status() {
            order.updateStatus(null);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("CompanyOrder 상태 전환")
    class CompanyOrderStatusTransition {

        @Test
        @DisplayName("기본 생성 상태 = ORDERED")
        void default_status_is_ordered() {
            assertThat(companyOrder.getStatus()).isEqualTo(CompanyOrderStatus.ORDERED);
        }

        @Test
        @DisplayName("prepare() → PREPARING")
        void prepare_changes_status_to_preparing() {
            companyOrder.prepare();
            assertThat(companyOrder.getStatus()).isEqualTo(CompanyOrderStatus.PREPARING);
        }

        @Test
        @DisplayName("ship() → SHIPPED")
        void ship_changes_status_to_shipped() {
            companyOrder.prepare();
            companyOrder.ship();
            assertThat(companyOrder.getStatus()).isEqualTo(CompanyOrderStatus.SHIPPED);
        }

        @Test
        @DisplayName("deliver() → DELIVERED")
        void deliver_changes_status_to_delivered() {
            companyOrder.prepare();
            companyOrder.ship();
            companyOrder.deliver();
            assertThat(companyOrder.getStatus()).isEqualTo(CompanyOrderStatus.DELIVERED);
        }

        @Test
        @DisplayName("cancel() → CANCELLED + soft delete 처리")
        void cancel_sets_cancelled_and_soft_deletes() {
            UUID requesterId = UUID.randomUUID();
            companyOrder.cancel(requesterId);

            assertThat(companyOrder.getStatus()).isEqualTo(CompanyOrderStatus.CANCELLED);
            assertThat(companyOrder.getDeletedAt()).isNotNull();
            assertThat(companyOrder.getDeletedBy()).isEqualTo(requesterId);
        }
    }
}
