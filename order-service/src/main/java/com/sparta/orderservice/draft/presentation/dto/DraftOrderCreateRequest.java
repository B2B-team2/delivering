package com.sparta.orderservice.draft.presentation.dto;

import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DraftOrderCreateRequest(
    @NotEmpty List<UUID> draftIds,          // 주문으로 전환할 임시주문 항목 ID 목록

    // 수령인 정보 — nullable 허용
    // 값이 있으면 그대로 사용, null이면 Company Service 기본 배송지(is_default=true) 자동 조회
    // 프론트엔드가 있다면 기본 배송지를 pre-fill 후 사용자가 수정하는 흐름
    String address,
    String recipientName,
    String phone,

    String slackId,                         // nullable

    @NotNull @Future LocalDateTime dueDate,
    String requestMemo,                     // nullable

    UUID receiverCompanyId                  // MASTER만 body로 전달, COMPANY_MANAGER는 X-Company-Id 헤더에서 주입
) {
    public CreateOrderFromDraftCommand toCommand(UUID userId, UUID receiverCompanyId) {
        return new CreateOrderFromDraftCommand(
                userId,
                receiverCompanyId,
                draftIds,
                address,
                recipientName,
                phone,
                slackId,
                dueDate,
                requestMemo
        );
    }
}
