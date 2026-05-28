package com.sparta.hubservice.inventory;

import com.sparta.common.dto.BusinessException;
import com.sparta.hubservice.global.client.OrderClient;
import com.sparta.hubservice.global.exception.ErrorCode;
import com.sparta.hubservice.inventory.application.service.InventoryService;
import com.sparta.hubservice.inventory.application.service.InventoryShipmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * ShipmentService Saga 단위 테스트
 *
 * Saga 흐름:
 * (1) 재고 차감 (deductStockByCompanyOrder)
 * (2) Order Service 출고 완료 호출 (shipCompanyOrder)
 * (3) (2) 실패 시 보상: 재고 복원 (revertDeductByCompanyOrder)
 */
@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock private InventoryService inventoryService;
    @Mock private OrderClient orderClient;
    @InjectMocks private InventoryShipmentService shipmentService;

    @Test
    void 출고_성공_재고차감_후_출고완료_호출() {
        UUID companyOrderId = UUID.randomUUID();

        doNothing().when(inventoryService).deductStockByCompanyOrder(companyOrderId);
        doNothing().when(orderClient).shipCompanyOrder(companyOrderId);

        shipmentService.ship(companyOrderId);

        verify(inventoryService).deductStockByCompanyOrder(companyOrderId);
        verify(orderClient).shipCompanyOrder(companyOrderId);
        verify(inventoryService, never()).revertDeductByCompanyOrder(any());
    }

    @Test
    void 출고완료_호출_실패시_재고복원_보상_실행() {
        UUID companyOrderId = UUID.randomUUID();

        doNothing().when(inventoryService).deductStockByCompanyOrder(companyOrderId);
        doThrow(new RuntimeException("Order Service 호출 실패"))
                .when(orderClient).shipCompanyOrder(companyOrderId);
        doNothing().when(inventoryService).revertDeductByCompanyOrder(companyOrderId);

        assertThatThrownBy(() -> shipmentService.ship(companyOrderId))
                .isInstanceOf(RuntimeException.class);

        verify(inventoryService).deductStockByCompanyOrder(companyOrderId);
        verify(orderClient).shipCompanyOrder(companyOrderId);
        verify(inventoryService).revertDeductByCompanyOrder(companyOrderId);
    }

    @Test
    void 재고차감_실패시_출고완료_호출_안함() {
        UUID companyOrderId = UUID.randomUUID();

        doThrow(new BusinessException(ErrorCode.INVENTORY_NOT_FOUND))
                .when(inventoryService).deductStockByCompanyOrder(companyOrderId);

        assertThatThrownBy(() -> shipmentService.ship(companyOrderId))
                .isInstanceOf(BusinessException.class);

        verify(orderClient, never()).shipCompanyOrder(any());
        verify(inventoryService, never()).revertDeductByCompanyOrder(any());
    }

    @Test
    void 보상_실패해도_원래_예외_전파() {
        UUID companyOrderId = UUID.randomUUID();

        doNothing().when(inventoryService).deductStockByCompanyOrder(companyOrderId);
        doThrow(new RuntimeException("Order Service 호출 실패"))
                .when(orderClient).shipCompanyOrder(companyOrderId);
        doThrow(new RuntimeException("보상도 실패"))
                .when(inventoryService).revertDeductByCompanyOrder(companyOrderId);

        assertThatThrownBy(() -> shipmentService.ship(companyOrderId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Order Service 호출 실패");

        verify(inventoryService).revertDeductByCompanyOrder(companyOrderId);
    }
}
