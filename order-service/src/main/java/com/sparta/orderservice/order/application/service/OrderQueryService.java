package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.CompanyOrderResult;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import com.sparta.orderservice.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final OrderRepository orderRepository;
    private final CompanyOrderRepository companyOrderRepository;

    /**
     * 전체 주문 조회 (페이징)
     */
    public Page<OrderResult> getOrders(UUID requesterId, Pageable pageable) {
        return orderRepository.findAllOrders(pageable).map(OrderResult::from);
    }

    /**
     * 주문 단건 상세 조회
     */
    public OrderResult getOrder(UUID orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        return OrderResult.from(order);
    }

    /**
     * 서브 주문 상세 조회
     */
    public CompanyOrderResult getCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = companyOrderRepository.findCompanyOrderWithItemsAndOrder(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
        return CompanyOrderResult.from(companyOrder);
    }

    /**
     * 결제 취소 가능 여부 조회
     */
    public boolean isCancellable(UUID orderId) {
        return orderRepository.findOrderById(orderId)
                .map(this::checkOrderCancellable)
                .orElse(false);
    }

    private boolean checkOrderCancellable(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) return false;
        return order.getCompanyOrders().stream()
                .noneMatch(co -> co.getStatus() == CompanyOrderStatus.SHIPPED
                        || co.getStatus() == CompanyOrderStatus.DELIVERED);
    }
}
