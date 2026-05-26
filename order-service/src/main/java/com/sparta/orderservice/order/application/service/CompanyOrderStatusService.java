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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 배송 서비스 내부 호출 전용 — 사용자 인증 없음, 단순 상태 전환
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyOrderStatusService {

    private final CompanyOrderRepository companyOrderRepository;
    private final HubStockPort hubStockPort;

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

    // 출고 완료: PREPARING → SHIPPED, Order → DELIVERING
    @Transactional
    public CompanyOrderResult shipCompanyOrder(UUID companyOrderId) {
        CompanyOrder companyOrder = findWithItemsAndOrderOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.PREPARING) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.ship();

        Order order = companyOrder.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.startDelivery();
        }

        // 실재고 차감 (마지막 외부 호출 → 실패 시 TX 롤백으로 DB 복원)
        hubStockPort.deductStock(companyOrder);

        return CompanyOrderResult.from(companyOrder);
    }

    // 업체 주문 수령 완료: SHIPPED → DELIVERED
    @Transactional
    public CompanyOrderDeliveredResult confirmDelivery(UUID companyOrderId) {
        CompanyOrder companyOrder = findWithOrderAndSiblingsOrThrow(companyOrderId);
        if (companyOrder.getStatus() != CompanyOrderStatus.SHIPPED) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }
        companyOrder.deliver();
        companyOrder.getOrder().updateStatus(null);
        return CompanyOrderDeliveredResult.from(companyOrder);
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
