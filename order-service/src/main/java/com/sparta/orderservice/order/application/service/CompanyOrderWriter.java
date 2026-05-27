package com.sparta.orderservice.order.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.core.CompanyOrderStatus;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.domain.core.OrderStatus;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CompanyOrderWriter {

    private final CompanyOrderRepository companyOrderRepository;

    /**
     * PREPARING → SHIPPED + Order → DELIVERING 상태를 독립 TX로 커밋
     */
    @Transactional
    public CompanyOrder persistShip(UUID companyOrderId) {
        CompanyOrder co = companyOrderRepository.findCompanyOrderWithItemsAndOrder(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));

        if (co.getStatus() != CompanyOrderStatus.PREPARING) {
            throw new BusinessException(OrderErrorCode.INVALID_STATUS_TRANSITION);
        }

        co.ship();
        Order order = co.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.startDelivery();
        }

        // TX 종료 후 deductStock 인자로 사용하기 위해 지연 로딩 컬렉션 미리 초기화
        co.getOrderItems().size();

        return co;
    }

    /**
     * deductStock 외부 호출 실패 시 SHIPPED → PREPARING 복원 (Saga 보상 전용)
     * : SHIPPED 상태인 CompanyOrder가 하나도 없어지면 Order도 PENDING으로 복원
     */
    @Transactional
    public void revertShip(UUID companyOrderId) {
        CompanyOrder co = companyOrderRepository.findCompanyOrderWithItemsAndOrder(companyOrderId)
                .orElseThrow(() -> new BusinessException(OrderErrorCode.COMPANY_ORDER_NOT_FOUND));

        if (co.getStatus() != CompanyOrderStatus.SHIPPED) {
            return; // 이미 다른 경로로 복원됐거나 상태가 변경된 경우
        }

        co.revertShip();

        Order order = co.getOrder();
        boolean anyOtherShipped = order.getCompanyOrders().stream()
                .anyMatch(other -> !other.getCompanyOrderId().equals(companyOrderId)
                        && other.getStatus() == CompanyOrderStatus.SHIPPED);

        if (!anyOtherShipped && order.getStatus() == OrderStatus.DELIVERING) {
            order.revertDelivery();
        }
    }
}
