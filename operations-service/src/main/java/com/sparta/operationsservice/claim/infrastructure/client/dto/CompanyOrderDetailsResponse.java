package com.sparta.operationsservice.claim.infrastructure.client.dto;

import java.util.List;
import java.util.UUID;

public record CompanyOrderDetailsResponse(
        UUID orderId,
        UUID companyOrderId,
        List<ItemDetails> items
) {
    public record ItemDetails(
            UUID productOptionId,
            int quantity
    ) {}
}
