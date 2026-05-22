package com.sparta.orderservice.draft.application.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CreateOrderFromDraftCommand(
        UUID userId,
        List<UUID> draftIds,            // 주문으로 전환할 임시주문 항목 ID 목록
        UUID deliveryAddressId,         // 배송지 ID (Delivery Service 연동용)
        String address,                 // 배송지 JSON 문자열 {"address": "기본주소", "address_detail": "상세주소"}
        String recipientName,
        String phone,
        String slackId,                 // nullable
        LocalDateTime dueDate,
        String requestMemo              // nullable
) {}
