package com.sparta.orderservice.draft.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.application.dto.AddDraftCommand;
import com.sparta.orderservice.draft.application.dto.CreateOrderFromDraftCommand;
import com.sparta.orderservice.draft.application.dto.DraftResult;
import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import com.sparta.orderservice.order.application.dto.OrderResult;
import com.sparta.orderservice.order.application.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DraftService {

    private final DraftRepository draftRepository;
    private final OrderService orderService;

    /**
     * 임시주문 항목 추가 (upsert)
     * - 동일 userId + productOptionId가 존재하면 복원 후 수량 갱신
     * - 없으면 새로 INSERT
     */
    @Transactional
    public DraftResult addDraft(AddDraftCommand command) {
        Optional<Draft> existing = draftRepository.findExistingDraft(command.userId(), command.productOptionId());

        Draft draft;
        if (existing.isPresent()) {
            draft = existing.get();
            draft.restore(command.quantity());
        } else {
            draft = draftRepository.save(Draft.of(command.userId(), command.productId(), command.productOptionId(), command.quantity()));
        }

        return DraftResult.from(draft);
    }

    // 임시주문 목록 조회 (페이징)
    // TODO: 권한별 필터링 (마스터 → 전체, 업체담당자 → 본인 것만)
    public Page<DraftResult> getDrafts(UUID userId, Pageable pageable) {
        return draftRepository.findDraftsByUserId(userId, pageable)
                .map(DraftResult::from);
    }

    // 임시주문 항목 수량 수정
    @Transactional
    public DraftResult updateDraft(UUID draftId, int quantity, UUID userId) {
        Draft draft = findDraftOrThrow(draftId);
        checkOwnership(draft, userId);
        draft.updateQuantity(quantity);
        return DraftResult.from(draft);
    }

    // 임시주문 항목 삭제 (soft delete)
    @Transactional
    public void deleteDraft(UUID draftId, UUID userId) {
        Draft draft = findDraftOrThrow(draftId);
        checkOwnership(draft, userId);
        draft.delete(userId);
    }

    // 임시주문으로 주문 생성
    @Transactional
    public OrderResult createOrderFromDraft(CreateOrderFromDraftCommand command) {
        // 1. 임시주문 항목 조회 및 소유권 검증
        List<Draft> drafts = command.draftIds().stream()
                .map(this::findDraftOrThrow)
                .peek(draft -> checkOwnership(draft, command.userId()))
                .toList();

        // TODO: Product Service FeignClient로 productOptionId → (companyId, unitPrice) 조회

        // TODO: companyId 기준으로 그룹핑 → CompanyOrderCommand 목록 생성

        // TODO: receiverCompanyId 주입 (X-Company-Id 헤더 또는 User Service 연동 확정 후 처리)

        // TODO: Company Service FeignClient로 receiverCompanyId → 기본 배송지(is_default=true) 조회
        //       → address, recipientName, phone 자동 세팅
        //       직접 주문 생성(POST /api/v1/orders)은 바디에 직접 입력하는 방식으로 테스트

        throw new UnsupportedOperationException("Hub Service FeignClient 연동 후 구현 예정");

        // 주문 생성
        // OrderResult orderResult = orderService.createOrder(new CreateOrderCommand(...), command.userId());

        // 임시주문 항목 soft delete
        // drafts.forEach(draft -> draft.delete(command.userId().toString()));

        // return orderResult;
    }

    private Draft findDraftOrThrow(UUID draftId) {
        return draftRepository.findDraftById(draftId)
                .orElseThrow(() -> new BusinessException(DraftErrorCode.DRAFT_NOT_FOUND));
    }

    // 본인 임시주문 항목인지 검증
    private void checkOwnership(Draft draft, UUID userId) {
        if (!draft.getUserId().equals(userId)) {
            throw new BusinessException(DraftErrorCode.DRAFT_ACCESS_DENIED);
        }
    }
}
