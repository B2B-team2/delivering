package com.sparta.orderservice.draft.infrastructure.repository;

import com.sparta.orderservice.draft.domain.core.Draft;
import com.sparta.orderservice.draft.domain.repository.DraftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class DraftRepositoryImpl implements DraftRepository {

    // JPA 구현체 — 이 클래스 내부에서만 사용
    private final DraftJpaRepository draftJpaRepository;

    @Override
    public Draft save(Draft draft) {
        return draftJpaRepository.save(draft);
    }

    @Override
    public Optional<Draft> findDraftById(UUID draftId) {
        return draftJpaRepository.findByDraftIdAndDeletedAtIsNull(draftId);
    }

    @Override
    public Page<Draft> findDraftsByUserId(UUID userId, Pageable pageable) {
        return draftJpaRepository.findAllByUserIdAndDeletedAtIsNull(userId, pageable);
    }

    @Override
    public Page<Draft> findAllDrafts(Pageable pageable) {
        return draftJpaRepository.findAllByDeletedAtIsNull(pageable);
    }

    @Override
    public Optional<Draft> findExistingDraft(UUID userId, UUID productOptionId) {
        // soft-deleted 포함 조회 (deletedAt 조건 없이)
        return draftJpaRepository.findByUserIdAndProductOptionId(userId, productOptionId);
    }

    @Override
    public void delete(Draft draft) {
        draftJpaRepository.delete(draft);
    }
}
