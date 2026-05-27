package com.sparta.operationsservice.claim.infrastructure.client;

import com.sparta.operationsservice.claim.application.port.OrderPort;
import com.sparta.operationsservice.claim.infrastructure.client.dto.CompanyOrderDetailsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderAdapter implements OrderPort {

    private final OrderClient orderClient;

    @Override
    public CompanyOrderDetails getCompanyOrderDetails(UUID companyOrderId) {
        CompanyOrderDetailsResponse response = orderClient.getCompanyOrderDetails(companyOrderId);
        return new CompanyOrderDetails(
                response.orderId(),
                response.companyOrderId(),
                response.items().stream()
                        .map(item -> new CompanyOrderDetails.ItemDetails(item.productOptionId(), item.quantity()))
                        .collect(Collectors.toList())
        );
    }

    @Override
    public void cancelCompanyOrderByClaim(UUID companyOrderId, UUID adminId) {
        orderClient.cancelCompanyOrderByClaim(companyOrderId, adminId);
    }
}
