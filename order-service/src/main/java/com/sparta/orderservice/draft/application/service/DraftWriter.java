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

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DraftWriter {

    private final DraftRepository draftRepository;

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
