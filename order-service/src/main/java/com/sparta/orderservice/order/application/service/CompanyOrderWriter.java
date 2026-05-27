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

        return co;
    }
}
