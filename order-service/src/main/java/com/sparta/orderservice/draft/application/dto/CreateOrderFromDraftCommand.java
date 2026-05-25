package com.sparta.orderservice.draft.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderFromDraftCommand(
        UUID userId,
        UUID receiverCompanyId,         // TODO: X-Company-Id 헤더로 주입 예정 (인증 확정 후)
        List<UUID> draftIds,            // 주문으로 전환할 임시주문 항목 ID 목록
        String address,                 // 배송지 JSON 문자열 {"address": "기본주소", "address_detail": "상세주소"}
        String recipientName,
        String phone,
        String slackId,                 // nullable
        LocalDateTime dueDate,
        String requestMemo              // nullable
) {}
