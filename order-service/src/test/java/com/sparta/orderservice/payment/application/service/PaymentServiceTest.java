package com.sparta.orderservice.payment.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.order.application.service.OrderCommandService;
import com.sparta.orderservice.order.application.service.OrderQueryService;
import com.sparta.orderservice.payment.application.dto.PaymentResult;
import com.sparta.orderservice.payment.domain.core.Payment;
import com.sparta.orderservice.payment.domain.core.PaymentMethod;
import com.sparta.orderservice.payment.domain.core.PaymentStatus;
import com.sparta.orderservice.payment.domain.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService 단위 테스트")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private OrderQueryService orderQueryService;
    @Mock
    private OrderCommandService orderCommandService;

    @InjectMocks
    private PaymentService paymentService;

    private UUID paymentId;
    private UUID orderId;
    private UUID requesterId;
    private Payment completedPayment;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        paymentId = UUID.randomUUID();
        requesterId = UUID.randomUUID();
        completedPayment = Payment.complete(orderId, PaymentMethod.CARD, BigDecimal.valueOf(20000));
    }

    @Nested
    @DisplayName("cancelPayment()")
    class CancelPayment {

        @Test
        @DisplayName("정상 취소: cancelOrder 위임 후 재조회 결과 반환")
        void success_cancels_order_and_returns_refetched_payment() {
            Payment cancelledPayment = Payment.complete(orderId, PaymentMethod.CARD, BigDecimal.valueOf(20000));
            cancelledPayment.cancel(requesterId);

            when(paymentRepository.findPaymentById(paymentId))
                    .thenReturn(Optional.of(completedPayment))  // 첫 번째 조회 (검증용)
                    .thenReturn(Optional.of(cancelledPayment)); // 두 번째 조회 (재조회)
            when(orderQueryService.isCancellable(orderId)).thenReturn(true);
            doNothing().when(orderCommandService).cancelOrder(any(), any());

            PaymentResult result = paymentService.cancelPayment(paymentId, requesterId);

            verify(orderCommandService).cancelOrder(orderId, requesterId);
            verify(paymentRepository, times(2)).findPaymentById(paymentId);
            assertThat(result.status()).isEqualTo(PaymentStatus.CANCELLED.name());
        }

        @Test
        @DisplayName("이미 CANCELLED 상태 → 예외 발생, cancelOrder 미호출")
        void already_cancelled_throws_without_calling_cancel_order() {
            Payment cancelledPayment = Payment.complete(orderId, PaymentMethod.CARD, BigDecimal.valueOf(20000));
            cancelledPayment.cancel(requesterId);
            when(paymentRepository.findPaymentById(paymentId)).thenReturn(Optional.of(cancelledPayment));

            assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, requesterId))
                    .isInstanceOf(BusinessException.class);

            verify(orderCommandService, never()).cancelOrder(any(), any());
        }

        @Test
        @DisplayName("주문 취소 불가 상태 → 예외 발생, cancelOrder 미호출")
        void order_not_cancellable_throws_without_calling_cancel_order() {
            when(paymentRepository.findPaymentById(paymentId)).thenReturn(Optional.of(completedPayment));
            when(orderQueryService.isCancellable(orderId)).thenReturn(false);

            assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, requesterId))
                    .isInstanceOf(BusinessException.class);

            verify(orderCommandService, never()).cancelOrder(any(), any());
        }

        @Test
        @DisplayName("결제 없음 → 예외 발생")
        void payment_not_found_throws() {
            when(paymentRepository.findPaymentById(paymentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> paymentService.cancelPayment(paymentId, requesterId))
                    .isInstanceOf(BusinessException.class);
        }
    }

    @Nested
    @DisplayName("createCompletedPayment()")
    class CreateCompletedPayment {

        @Test
        @DisplayName("COMPLETED 상태 Payment 생성 및 저장")
        void creates_completed_payment_and_saves() {
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            PaymentResult result = paymentService.createCompletedPayment(orderId, BigDecimal.valueOf(20000));

            verify(paymentRepository).save(any());
            assertThat(result.status()).isEqualTo(PaymentStatus.COMPLETED.name());
            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.amount()).isEqualByComparingTo(BigDecimal.valueOf(20000));
        }
    }

    @Nested
    @DisplayName("cancelPaymentByOrderId()")
    class CancelPaymentByOrderId {

        @Test
        @DisplayName("COMPLETED 결제 → CANCELLED 처리")
        void completed_payment_gets_cancelled() {
            when(paymentRepository.findPaymentByOrderId(orderId)).thenReturn(Optional.of(completedPayment));

            paymentService.cancelPaymentByOrderId(orderId, requesterId);

            assertThat(completedPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("이미 CANCELLED 결제 → 상태 변경 없음 (idempotent)")
        void already_cancelled_payment_is_idempotent() {
            completedPayment.cancel(requesterId);
            when(paymentRepository.findPaymentByOrderId(orderId)).thenReturn(Optional.of(completedPayment));

            paymentService.cancelPaymentByOrderId(orderId, requesterId);

            assertThat(completedPayment.getStatus()).isEqualTo(PaymentStatus.CANCELLED);
        }

        @Test
        @DisplayName("결제 없는 orderId → 예외 없이 종료")
        void no_payment_for_order_does_nothing() {
            when(paymentRepository.findPaymentByOrderId(orderId)).thenReturn(Optional.empty());

            paymentService.cancelPaymentByOrderId(orderId, requesterId);
            // 예외 없이 정상 종료
        }
    }
}
