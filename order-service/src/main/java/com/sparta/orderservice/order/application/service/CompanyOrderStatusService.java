package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.ClaimCancelResult;
import com.sparta.orderservice.order.application.dto.CompanyOrderResult;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.event.OrderCancelledEvent;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 배송 서비스 내부 호출 전용 — 사용자 인증 없음, 단순 상태 전환
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyOrderStatusService {

    private final CompanyOrderRepository companyOrderRepository;
    private final CompanyOrderWriter companyOrderWriter;
    private final ApplicationEventPublisher eventPublisher;

    // 출고 준비 확인: ORDERED → PREPARING
    @Transactional
    public CompanyOrderResult prepareCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = findWithItemsAndOrderOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.ORDERED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.prepare();
        return CompanyOrderResult.from(companyOrder);
    }

    /**
     * 출고 완료: PREPARING → SHIPPED, Order → DELIVERING
     */
    @Transactional
    public CompanyOrderResult shipCompanyOrder(UUID companyOrderId) {
        return CompanyOrderResult.from(companyOrderWriter.persistShip(companyOrderId));
    }

    // 업체 주문 수령 완료: SHIPPED → DELIVERED
    // 모든 CompanyOrder가 DELIVERED이면 Order → COMPLETED 자동 전환
    @Transactional
    public void confirmDelivery(UUID companyOrderId, UUID requesterId) {
        CompanyOrder companyOrder = findWithOrderAndSiblingsOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.SHIPPED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.deliver();
        companyOrder.getOrder().updateStatus(requesterId);
    }

    /**
     * 클레임 처리에 의한 서브 주문 취소 (SHIPPED 또는 DELIVERED → CANCELLED)
     * operations-service 내부 호출 전용 — 재고/배송 보상 없이 상태 변경만 수행
     * 모든 CompanyOrder가 CANCELLED이면 Order → CANCELLED & 결제 취소 이벤트 발행
     */
    @Transactional
    public ClaimCancelResult cancelCompanyOrderByClaim(UUID companyOrderId, UUID requesterId) {
        CompanyOrder companyOrder = findWithOrderAndSiblingsOrThrow(companyOrderId);
        CompanyOrderStatus status = companyOrder.getStatus();
        if (status != CompanyOrderStatus.SHIPPED && status != CompanyOrderStatus.DELIVERED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        // 서브 주문 취소
        companyOrder.cancel(requesterId);

        // 상위 주문 상태 업데이트 (모든 서브 주문이 취소되면 상위 주문도 취소됨)
        companyOrder.getOrder().updateStatus(requesterId);

        // 상위 주문이 최종 취소 상태가 되면 결제 취소 이벤트 발행
        if (companyOrder.getOrder().getStatus() == OrderStatus.CANCELLED) {
            eventPublisher.publishEvent(new OrderCancelledEvent(companyOrder.getOrder().getOrderId(), requesterId));
        }

        return ClaimCancelResult.from(companyOrder);
    }

    private CompanyOrder findWithItemsAndOrderOrThrow(UUID companyOrderId) {
        return companyOrderRepository.findCompanyOrderWithItemsAndOrder(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
    }

    private CompanyOrder findWithOrderAndSiblingsOrThrow(UUID companyOrderId) {
        return companyOrderRepository.findCompanyOrderWithOrderAndSiblings(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));
    }
}
