package com.sparta.hubservice.inventory.application.service;

import com.sparta.hubservice.global.client.OrderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryShipmentService {

    private final InventoryService inventoryService;
    private final OrderClient orderClient;

    public void prepare(UUID companyOrderId) {
        orderClient.prepareCompanyOrder(companyOrderId);
    }

    /**
     * 출고 Saga:
     * (1) 재고 차감 DB 커밋 (InventoryService 독립 TX)
     * (2) Order Service 출고 완료 외부 호출
     * (3) 실패 시 보상: 재고 복원
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void ship(UUID companyOrderId) {
        inventoryService.deductStockByCompanyOrder(companyOrderId);

        try {
            orderClient.shipCompanyOrder(companyOrderId);
        } catch (Exception e) {
            log.error("[Saga] 출고 완료 호출 실패, 재고 복원 보상 실행: companyOrderId={}", companyOrderId, e);
            try {
                inventoryService.revertDeductByCompanyOrder(companyOrderId);
            } catch (Exception compensationEx) {
                log.error("[Saga] 재고 복원 실패 - 수동 복구 필요: companyOrderId={}", companyOrderId, compensationEx);
            }
            throw e;
        }
    }
}
