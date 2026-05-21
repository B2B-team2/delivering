package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.CompanyOrderResult;
import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderItem;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import com.sparta.orderservice.payment.application.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final CompanyOrderRepository companyOrderRepository;
    private final PaymentService paymentService;

    // 주문 생성
    @Transactional
    public OrderResult createOrder(CreateOrderCommand command, UUID requesterId) {
        // 전체 주문 금액 = 모든 업체 주문 항목의 (수량 × 단가) 합계
        BigDecimal totalPrice = command.companyOrders().stream()
                .flatMap(co -> co.orderItems().stream())
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.of(
                command.requesterCompanyId(),
                command.receiverCompanyId(),
                command.recipientName(),
                command.phone(),
                command.slackId(),
                command.address(),
                command.dueDate(),
                command.requestMemo(),
                totalPrice,
                BigDecimal.ZERO,    // todo 배송비계산... 허브연동시?
                totalPrice          // finalPrice = totalPrice + deliveryFee
        );

        for (CreateOrderCommand.CompanyOrderCommand coCmd : command.companyOrders()) {
            BigDecimal subtotal = coCmd.orderItems().stream()
                    .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CompanyOrder companyOrder = CompanyOrder.of(order, coCmd.companyId(), subtotal, BigDecimal.ZERO);

            for (CreateOrderCommand.OrderItemCommand itemCmd : coCmd.orderItems()) {
                OrderItem item = OrderItem.of(
                        companyOrder,
                        itemCmd.productOptionId(),
                        itemCmd.quantity(),
                        itemCmd.unitPrice()
                );
                companyOrder.getOrderItems().add(item);
            }
            order.getCompanyOrders().add(companyOrder);
        }

        orderRepository.save(order);
//
//        // 선결제: 주문 생성과 동시에 결제 COMPLETED 처리 (같은 트랜잭션)
//        paymentService.createCompletedPayment(order.getOrderId(), totalPrice);

        return OrderResult.from(order);
    }

    // 전체 주문 조회
    public List<OrderResult> getOrders(UUID requesterId) {
        // TODO: 권한별 필터링 (마스터/허브관리자 → 전체, 업체 담당자 → 자기 회사 주문만)
        return orderRepository.findAllOrders().stream()
                .map(OrderResult::from)
                .toList();
    }

    // 주문 단건 상세 조회
    public OrderResult getOrder(UUID orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResult.from(order);
    }

    // 주문 전체 취소
    @Transactional
    public void cancelOrder(UUID orderId, UUID requesterId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.getCompanyOrders().forEach(co -> co.cancel(requesterId.toString()));
        order.cancel(requesterId.toString());
    }

    // 서브 주문 상세 조회
    public CompanyOrderResult getCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = companyOrderRepository.findCompanyOrderById(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
        return CompanyOrderResult.from(companyOrder);
    }

    // 서브 주문 부분 취소
    @Transactional
    public void cancelCompanyOrder(UUID companyOrderId, UUID requesterId) {
        CompanyOrder companyOrder = companyOrderRepository.findCompanyOrderById(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
        companyOrder.cancel(requesterId.toString());
    }
}
