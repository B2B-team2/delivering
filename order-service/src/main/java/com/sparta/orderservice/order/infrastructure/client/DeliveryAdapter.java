package com.sparta.orderservice.order.infrastructure.client;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.global.exception.OrderErrorCode;
import com.sparta.orderservice.order.application.port.DeliveryPort;
import com.sparta.orderservice.order.domain.core.Order;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCancelRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateRequest;
import com.sparta.orderservice.order.infrastructure.client.dto.DeliveryCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryAdapter implements DeliveryPort {

    private final DeliveryClient deliveryClient;

    // 모든 CompanyOrder의 배송 요청을 리스트로 만들어 단 1회 호출
    @Override
    public Map<UUID, UUID> createDeliveries(Order order, Map<UUID, UUID> hubIdMap, UUID destinationHubId) {
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

            return deliveryClient.createDeliveries(requests).stream()
                    .collect(Collectors.toMap(
                            DeliveryCreateResponse::companyOrderId,
                            DeliveryCreateResponse::deliveryId
                    ));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("createDeliveries", e);
        }
    }

    // 배송 일괄 취소 (Saga 보상 전용): createOrder 실패 시 생성된 배송 전체 취소
    @Override
    public void cancelDeliveries(List<UUID> companyOrderIds) {
        try {
            List<DeliveryCancelRequest> requests = companyOrderIds.stream()
                    .map(DeliveryCancelRequest::new)
                    .toList();
            deliveryClient.cancelDeliveries(requests);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw handleUnexpectedException("cancelDeliveries", e);
        }
    }

    private RuntimeException handleUnexpectedException(String operation, Exception e) {
        log.error("[Delivery] Unexpected error [{}]", operation, e);
        return new BusinessException(OrderErrorCode.DELIVERY_SERVICE_UNAVAILABLE);
    }
}
