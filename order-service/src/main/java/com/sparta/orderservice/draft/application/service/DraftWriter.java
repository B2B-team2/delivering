package com.sparta.orderservice.draft.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.orderservice.draft.application.dto.AddDraftCommand;
import com.sparta.orderservice.draft.application.dto.DraftResult;
import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import com.sparta.orderservice.global.exception.DraftErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DraftWriter {

    private final DraftRepository draftRepository;

    // 임시주문 일괄 조회 + 소유권 검증 (독립 readOnly TX)
    // - N+1 방지: ID 개수만큼 단건 조회하던 것을 IN 쿼리 1회로 교체
    // - createOrderFromDraft의 NOT_SUPPORTED 컨텍스트에서 새 readOnly TX를 열기 위해 DraftWriter에 위임
    @Transactional(readOnly = true)
    public List<Draft> readAndValidateDrafts(List<UUID> draftIds, UUID userId) {
        List<Draft> drafts = draftRepository.findAllDraftsByIds(draftIds);
        if (drafts.size() != draftIds.size()) {
            throw new BusinessException(DraftErrorCode.DRAFT_NOT_FOUND);
        }
        drafts.forEach(draft -> {
            if (!draft.getUserId().equals(userId)) {
                throw new BusinessException(DraftErrorCode.DRAFT_ACCESS_DENIED);
            }
        });
        return drafts;
    }

    // 임시주문 일괄 soft delete (독립 TX)
    // - N+1 방지: IN 쿼리 1회로 관리 상태 엔티티 일괄 로드 → dirty checking으로 UPDATE
    @Transactional
    public void deleteAll(List<UUID> draftIds, UUID userId) {
        List<Draft> drafts = draftRepository.findAllDraftsByIds(draftIds);
        if (drafts.size() != draftIds.size()) {
            throw new BusinessException(DraftErrorCode.DRAFT_NOT_FOUND);
        }
        drafts.forEach(draft -> draft.delete(userId));
    }

    // 실제 upsert 처리 (독립 TX)
    @Transactional
    public DraftResult tryInsertOrUpdate(AddDraftCommand command) {
        Optional<Draft> existing = draftRepository.findExistingDraft(command.userId(), command.productOptionId());
        if (existing.isPresent()) {
            existing.get().restore(command.quantity());
            return DraftResult.from(existing.get());
        }
        Draft draft = draftRepository.save(
                Draft.of(command.userId(), command.productId(), command.productOptionId(), command.quantity()));
        return DraftResult.from(draft);
    }

    // 충돌 시 재조회 후 수량 갱신 (독립 TX)
    @Transactional
    public DraftResult findAndRestore(AddDraftCommand command) {
        Draft draft = draftRepository.findExistingDraft(command.userId(), command.productOptionId())
                .orElseThrow(() -> new BusinessException(DraftErrorCode.DRAFT_ALREADY_EXISTS));
        draft.restore(command.quantity());
        return DraftResult.from(draft);
    }
}
