package com.sparta.orderservice.draft.presentation.dto;

import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DraftOrderCreateRequest(
        @NotEmpty List<UUID> draftIds,          // 주문으로 전환할 장바구니 항목 ID 목록
        @NotNull UUID deliveryAddressId,        // 배송지 ID
        @NotBlank String address,               // JSON: {"address": "기본주소", "address_detail": "상세주소"}
        @NotBlank String recipientName,
        @NotBlank String phone,
        String slackId,                         // nullable
        @NotNull @Future LocalDateTime dueDate,
        String requestMemo                      // nullable
) {
    public CreateOrderFromDraftCommand toCommand(UUID userId) {
        return new CreateOrderFromDraftCommand(
                userId,
                draftIds,
                deliveryAddressId,
                address,
                recipientName,
                phone,
                slackId,
                dueDate,
                requestMemo
        );
    }
}
