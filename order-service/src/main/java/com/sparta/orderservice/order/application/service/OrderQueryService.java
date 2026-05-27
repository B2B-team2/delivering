package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.global.security.AuthContext;
import com.sparta.orderservice.order.application.dto.CompanyOrderResult;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.Order;
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
    private final AuthContext authContext;

    /**
     * 주문 목록 조회 (페이징)
     * MASTER           → 전체 조회
     * COMPANY_MANAGER  → 자기 회사(수령업체 OR 공급업체)가 참여한 주문만
     * 그 외             → 403
     */
    public Page<OrderResult> getOrders(Pageable pageable) {
        if (authContext.isMaster()) {
            return orderRepository.findAllOrders(pageable).map(OrderResult::from);
        }
        if (authContext.isCompanyManager()) {
            UUID companyId = authContext.getCompanyId();
            return orderRepository.findOrdersByCompanyId(companyId, pageable).map(OrderResult::from);
        }
        throw new BusinessException(OrderErrorCode.FORBIDDEN);
    }

    /**
     * 주문 단건 상세 조회
     * COMPANY_MANAGER → 자기 회사가 수령업체 또는 공급업체인 주문만 허용
     */
    public OrderResult getOrder(UUID orderId) {
        Order order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
        validateOrderReadAccess(order);
        return OrderResult.from(order);
    }

    /**
     * 서브 주문 상세 조회
     * COMPANY_MANAGER → 자기 회사가 공급업체인 CompanyOrder만 허용
     */
    public CompanyOrderResult getCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = companyOrderRepository.findCompanyOrderWithItemsAndOrder(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
        validateCompanyOrderAccess(companyOrder);
        return CompanyOrderResult.from(companyOrder);
    }

    // payment 도메인 내부 호출용 — 결제 취소 가능 여부 조회
    public boolean isCancellable(UUID orderId) {
        return orderRepository.findOrderById(orderId)
            .map(Order::isCancellable)
            .orElse(false);
    }

    // payment 도메인 내부 호출용 — orderId → receiverCompanyId 조회 (cancelPayment / getPayment 접근 권한 검증)
    public UUID getReceiverCompanyId(UUID orderId) {
        return orderRepository.findReceiverCompanyIdByOrderId(orderId)
            .orElseThrow(() -> new BusinessException(OrderErrorCode.ORDER_NOT_FOUND));
    }

    // 주문 읽기 접근 검증 — 자기 회사가 수령업체 OR 공급업체인지 확인
    private void validateOrderReadAccess(Order order) {
        if (authContext.isMaster()) return;
        if (authContext.isCompanyManager()) {
            UUID companyId = authContext.getCompanyId();
            boolean isRelated = order.getReceiverCompanyId().equals(companyId)
                    || order.getCompanyOrders().stream()
                            .anyMatch(co -> co.getCompanyId().equals(companyId));
            if (!isRelated) throw new BusinessException(OrderErrorCode.FORBIDDEN);
            return;
        }
        throw new BusinessException(OrderErrorCode.FORBIDDEN);
    }

    // CompanyOrder 읽기 접근 검증 — 자기 회사가 공급업체인지 확인
    private void validateCompanyOrderAccess(CompanyOrder companyOrder) {
        if (authContext.isMaster()) return;
        if (authContext.isCompanyManager()) {
            UUID companyId = authContext.getCompanyId();
            if (!companyOrder.getCompanyId().equals(companyId)) {
                throw new BusinessException(OrderErrorCode.FORBIDDEN);
            }
            return;
        }
        throw new BusinessException(OrderErrorCode.FORBIDDEN);
    }
}
