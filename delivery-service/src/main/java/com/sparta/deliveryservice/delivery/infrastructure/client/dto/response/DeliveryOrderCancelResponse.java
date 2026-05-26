package com.sparta.deliveryservice.delivery.infrastructure.client.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryOrderCancelResponse {
    private UUID orderId;
    private int cancelledCount;
    private int skippedCount;
    private List<CancelledDeliveryDto> cancelledDeliveries;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelledDeliveryDto {
        private UUID deliveryId;
        private UUID companyOrderId;
        private String previousStatus;
    }
}