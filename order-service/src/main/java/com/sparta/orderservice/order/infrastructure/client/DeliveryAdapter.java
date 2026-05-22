package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.port.DeliveryPort;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryAdapter implements DeliveryPort {

    private final DeliveryClient deliveryClient;

    // 모든 CompanyOrder의 배송 요청을 리스트로 만들어 단 1회 호출
    @Override
    public void createDeliveries(Order order, Map<UUID, UUID> hubIdMap, UUID destinationHubId) {
        try {
            List<DeliveryCreateRequest> requests = order.getCompanyOrders().stream()
                    .map(companyOrder -> new DeliveryCreateRequest(
                            companyOrder.getCompanyOrderId(),
                            hubIdMap.get(companyOrder.getCompanyId()), // departureHubId (공급업체 소속 허브)
                            destinationHubId,                          // destinationHubId (수령업체 소속 허브)
                            order.getAddress(),
                            order.getRecipientName(),
                            order.getSlackId(),
                            order.getRequestMemo()
                    ))
                    .toList();
            deliveryClient.createDeliveries(requests);
        } catch (FeignException e) {
            handleDeliveryFeignException("createDeliveries", e);
        } catch (Exception e) {
            handleDeliveryUnexpectedException("createDeliveries", e);
        }
    }

    private void handleDeliveryFeignException(String operation, FeignException e) {
        log.error("Delivery service error [{}]: status={}", operation, e.status());
        throw new BusinessException(OrderErrorCode.DELIVERY_SERVICE_UNAVAILABLE);
    }

    private void handleDeliveryUnexpectedException(String operation, Exception e) {
        log.error("Unexpected error [{}]", operation, e);
        throw new BusinessException(OrderErrorCode.DELIVERY_SERVICE_UNAVAILABLE);
    }
}
