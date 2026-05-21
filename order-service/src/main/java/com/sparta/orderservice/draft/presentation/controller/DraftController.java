package com.sparta.orderservice.draft.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.common.dto.PageResponse;
import com.sparta.orderservice.draft.application.service.DraftService;
import com.sparta.orderservice.draft.presentation.dto.DraftAddRequest;
import com.sparta.orderservice.draft.presentation.dto.DraftOrderCreateRequest;
import com.sparta.orderservice.draft.presentation.dto.DraftResponse;
import com.sparta.orderservice.draft.presentation.dto.DraftUpdateRequest;
import com.sparta.orderservice.order.presentation.dto.OrderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;

    // 임시주문 항목 추가 (upsert: 동일 상품 옵션이면 복원 후 수량 갱신, 없으면 신규 추가)
    @PostMapping
    public ResponseEntity<ApiResponse<DraftResponse>> addDraft(
            @RequestBody @Valid DraftAddRequest request,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        DraftResponse response = DraftResponse.from(draftService.addDraft(request.toCommand(userId)));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 임시주문 목록 조회
    // TODO: 권한별 필터링 (마스터 → 전체, 업체담당자 → 본인 것만)
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<DraftResponse>>> getDrafts(
            @RequestHeader("X-User-Id") UUID userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<DraftResponse> response = draftService.getDrafts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 임시주문 항목 수량 수정
    @PatchMapping("/{draftId}")
    public ResponseEntity<ApiResponse<DraftResponse>> updateDraft(
            @PathVariable UUID draftId,
            @RequestBody @Valid DraftUpdateRequest request,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        DraftResponse response = DraftResponse.from(draftService.updateDraft(draftId, request.quantity(), userId));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 임시주문 항목 삭제
    @DeleteMapping("/{draftId}")
    public ResponseEntity<ApiResponse<Void>> deleteDraft(
            @PathVariable UUID draftId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        draftService.deleteDraft(draftId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 임시주문으로 주문 생성
    // TODO: Hub Service FeignClient로 productOptionId → companyId, unitPrice 조회 연동
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrderFromDraft(
            @RequestBody @Valid DraftOrderCreateRequest request,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        OrderResponse response = OrderResponse.from(draftService.createOrderFromDraft(request.toCommand(userId)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response));
    }
}
