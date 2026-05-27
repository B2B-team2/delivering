package com.sparta.operationsservice.claim.application.port;

import java.util.List;
import java.util.UUID;

public interface OrderPort {
    CompanyOrderDetails getCompanyOrderDetails(UUID companyOrderId);
    void cancelCompanyOrderByClaim(UUID companyOrderId, UUID adminId);

    record CompanyOrderDetails(
            UUID orderId,
            UUID companyOrderId,
            List<ItemDetails> items
    ) {
        public record ItemDetails(
                UUID productOptionId,
                int quantity
        ) {}
    }
}
