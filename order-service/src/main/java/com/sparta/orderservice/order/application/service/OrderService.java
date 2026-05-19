package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderErrorCode;
import com.sparta.orderservice.order.domain.core.OrderItem;
import com.sparta.orderservice.order.infrastructure.repository.CompanyOrderJpaRepository;
import com.sparta.orderservice.order.infrastructure.repository.OrderJpaRepository;
import com.sparta.orderservice.order.presentation.dto.CompanyOrderResponse;
import com.sparta.orderservice.order.presentation.dto.OrderCreateRequest;
import com.sparta.orderservice.order.presentation.dto.OrderResponse;
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

    private final OrderJpaRepository orderJpaRepository;
    private final CompanyOrderJpaRepository companyOrderJpaRepository;

    // 주문 생성
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request, UUID requesterId) {
        // 전체 주문 금액 = 모든 업체 주문 항목의 (수량 × 단가) 합계
        BigDecimal totalPrice = request.companyOrders().stream()
                .flatMap(co -> co.orderItems().stream())
                .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.of(
                request.requesterCompanyId(),
                request.receiverCompanyId(),
                request.recipientName(),
                request.phone(),
                request.slackId(),
                request.address(),
                request.dueDate(),
                request.requestMemo(),
                totalPrice,
                BigDecimal.ZERO,    // todo 배송비계산... 허브연동시?
                totalPrice          // finalPrice = totalPrice + deliveryFee
        );

        for (OrderCreateRequest.CompanyOrderRequest coReq : request.companyOrders()) {
            BigDecimal subtotal = coReq.orderItems().stream()
                    .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CompanyOrder companyOrder = CompanyOrder.of(order, coReq.companyId(), subtotal, BigDecimal.ZERO);

            for (OrderCreateRequest.OrderItemRequest itemReq : coReq.orderItems()) {
                OrderItem item = OrderItem.of(
                        companyOrder,
                        itemReq.productOptionId(),
                        itemReq.quantity(),
                        itemReq.unitPrice()
                );
                companyOrder.getOrderItems().add(item);
            }
            order.getCompanyOrders().add(companyOrder);
        }

        orderJpaRepository.save(order);
        return OrderResponse.from(order);
    }

    // 전체 주문 조회
    public List<OrderResponse> getOrders(UUID requesterId) {
        // TODO: 권한별 필터링 (마스터/허브관리자 → 전체, 업체 담당자 → 자기 회사 주문만)
        return orderJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(OrderResponse::from)
                .toList();
    }

    // 주문 단건 상세 조회
    public OrderResponse getOrder(UUID orderId) {
        Order order = orderJpaRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResponse.from(order);
    }

    // 주문 전체 취소
    @Transactional
    public void cancelOrder(UUID orderId, UUID requesterId) {
        Order order = orderJpaRepository.findByOrderIdAndDeletedAtIsNull(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        order.getCompanyOrders().forEach(co -> co.cancel(requesterId.toString()));
        order.cancel(requesterId.toString());
    }

    // 서브 주문 상세 조회
    public CompanyOrderResponse getCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = companyOrderJpaRepository.findByCompanyOrderIdAndDeletedAtIsNull(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
        return CompanyOrderResponse.from(companyOrder);
    }

    // 서브 주문 부분 취소
    @Transactional
    public void cancelCompanyOrder(UUID companyOrderId, UUID requesterId) {
        CompanyOrder companyOrder = companyOrderJpaRepository.findByCompanyOrderIdAndDeletedAtIsNull(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
        companyOrder.cancel(requesterId.toString());
    }
}
