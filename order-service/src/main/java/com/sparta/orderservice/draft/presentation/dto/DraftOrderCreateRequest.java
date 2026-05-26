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
        @NotEmpty List<UUID> draftIds,          // 주문으로 전환할 임시주문 항목 ID 목록
        // TODO: deliveryAddressId(UUID)를 받아 Company Service 내부 API로 조회 후 아래 필드 자동 세팅 예정
        //       확정 전까지는 클라이언트(Postman)에서 직접 입력 (p_delivery_addresses 테이블 활용 목적)
        @NotBlank String address,               // JSON: {"address": "기본주소", "address_detail": "상세주소"}
        @NotBlank String recipientName,
        @NotBlank String phone,
        String slackId,                         // nullable
        @NotNull @Future LocalDateTime dueDate,
        String requestMemo                      // nullable
) {
    // TODO: receiverCompanyId는 X-Company-Id 헤더로 주입 예정 (인증 확정 후)
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
