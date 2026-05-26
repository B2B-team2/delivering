package com.sparta.orderservice.order.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Presentation → Application 전달용 커맨드 객체
public record CreateOrderCommand(
        UUID receiverCompanyId,  // TODO: User service, 세션 기반 인증 확정 후 주입 예정
        String recipientName,
        String phone,
        String slackId,
        String address,
        LocalDateTime dueDate,
        String requestMemo,
        List<CompanyOrderCommand> companyOrders
) {
    public record CompanyOrderCommand(
            UUID companyId,
            List<OrderItemCommand> orderItems
    ) {}

    public record OrderItemCommand(
            UUID productOptionId,
            int quantity,
            BigDecimal unitPrice
    ) {}
}
