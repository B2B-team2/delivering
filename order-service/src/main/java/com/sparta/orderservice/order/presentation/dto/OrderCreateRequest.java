package com.sparta.orderservice.order.presentation.dto;

import com.sparta.orderservice.order.application.dto.CreateOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreateRequest(
        @NotNull UUID receiverCompanyId,            // 수령업체 TODO: X-Company-Id 헤더로 주입 예정
        @NotBlank String recipientName,             // 수령인 실명
        @NotBlank String phone,                     // 수령인 연락처
        String slackId,                             // 수령인 Slack ID (nullable)
        @NotBlank String address,                   // JSON: {"address": "기본주소", "address_detail": "상세주소"}
        @NotNull @Future LocalDateTime dueDate,     // 납품 기한
        String requestMemo,                         // 요청 사항 (nullable)
        @NotEmpty @Valid List<CompanyOrderRequest> companyOrders
) {

    public record CompanyOrderRequest(
            @NotNull UUID companyId,    // 공급업체 ID
            @NotEmpty @Valid List<OrderItemRequest> orderItems
    ) {}

    public record OrderItemRequest(
            @NotNull UUID productOptionId,              // 상품 옵션 ID
            @Positive int quantity,                     // 구매 수량
            @NotNull @Positive BigDecimal unitPrice     // 구매 시점 단가
    ) {}

    // Presentation → Application 변환 (Application 계층을 참조하는 방향은 허용)
    public CreateOrderCommand toCommand() {
        List<CreateOrderCommand.CompanyOrderCommand> companyOrderCommands = companyOrders.stream()
                .map(co -> new CreateOrderCommand.CompanyOrderCommand(
                        co.companyId(),
                        co.orderItems().stream()
                                .map(item -> new CreateOrderCommand.OrderItemCommand(
                                        item.productOptionId(),
                                        item.quantity(),
                                        item.unitPrice()
                                ))
                                .toList()
                ))
                .toList();

        return new CreateOrderCommand(
                receiverCompanyId,
                recipientName,
                phone,
                slackId,
                address,
                dueDate,
                requestMemo,
                companyOrderCommands
        );
    }
}
