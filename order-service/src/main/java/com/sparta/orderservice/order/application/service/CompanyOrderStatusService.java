package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.dto.CompanyOrderDeliveredResult;
import com.sparta.orderservice.order.application.dto.CompanyOrderResult;
import com.sparta.orderservice.order.application.port.HubStockPort;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// 배송 서비스 내부 호출 전용 — 사용자 인증 없음, 단순 상태 전환
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyOrderStatusService {

    private final CompanyOrderRepository companyOrderRepository;
    private final HubStockPort hubStockPort;
    private final CompanyOrderWriter companyOrderWriter;

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
     *
     * Saga 흐름:
     * (1) DB 상태를 SHIPPED로 먼저 커밋 (CompanyOrderWriter 독립 TX)
     * (2) TX 커밋 완료 후 재고 차감 외부 호출 (TX 외부)
     * (3) 외부 호출 실패 시 보상: DB를 PREPARING으로 복원
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CompanyOrderResult shipCompanyOrder(UUID companyOrderId) {
        // (1) SHIPPED + DELIVERING 상태를 독립 TX로 먼저 커밋
        CompanyOrder shippedOrder = companyOrderWriter.persistShip(companyOrderId);

        // (2) TX 커밋 완료 후 재고 차감
        try {
            hubStockPort.deductStock(shippedOrder);
        } catch (Exception e) {
            log.error("[Saga] 재고 차감 실패, PREPARING 복원 보상 실행: companyOrderId={}", companyOrderId, e);
            try {
                companyOrderWriter.revertShip(companyOrderId);
            } catch (Exception compensationEx) {
                log.error("[Saga] PREPARING 복원 실패 - 수동 복구 필요: companyOrderId={}", companyOrderId, compensationEx);
            }
            throw e;
        }

        return CompanyOrderResult.from(shippedOrder);
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
