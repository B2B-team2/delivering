package com.sparta.orderservice.order.application.service;

import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.event.OrderCreatedEvent;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OrderWriter {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 주문 저장 + 결제 이벤트 발행 (독립 TX)
     *
     * createOrder Saga의 마지막 단계:
     * - 재고 예약 / 배송 생성이 완료된 후 호출
     * - Order + CompanyOrder + OrderItem 전체를 단 하나의 TX로 저장 (CascadeType.ALL)
     * - OrderCreatedEvent → PaymentEventHandler → createCompletedPayment (같은 TX)
     *   → Payment 생성 실패 시 Order도 함께 롤백
     * - 실패 시 호출부(createOrder Saga catch)에서 외부 보상(재고 취소, 배송 취소) 실행
     */
    @Transactional
    public OrderResult saveOrderWithEvent(Order order) {
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(order.getOrderId(), order.getReceiverCompanyId(), order.getTotalPrice()));
        return OrderResult.from(order);
    }
}
