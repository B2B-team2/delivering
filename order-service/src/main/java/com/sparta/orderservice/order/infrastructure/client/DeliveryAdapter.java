package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.orderservice.order.application.port.DeliveryPort;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeliveryAdapter implements DeliveryPort {

    private final DeliveryClient deliveryClient;

    // 모든 CompanyOrder의 배송 요청을 리스트로 만들어 단 1회 호출
    @Override
    public void createDeliveries(Order order, Map<UUID, UUID> hubIdMap, UUID destinationHubId) {
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
    }
}
