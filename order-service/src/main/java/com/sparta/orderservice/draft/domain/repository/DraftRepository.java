package com.sparta.orderservice.draft.domain.repository;

import com.sparta.orderservice.draft.domain.core.Draft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DraftRepository {

    Draft save(Draft draft);

    Optional<Draft> findDraftById(UUID draftId);

    // N+1 방지용 일괄 조회 (soft-deleted 제외)
    List<Draft> findAllDraftsByIds(List<UUID> draftIds);

    Page<Draft> findDraftsByUserId(UUID userId, Pageable pageable);

    // 마스터 권한 전체 조회 (TODO: 권한별 필터링 구현 시 사용)
    Page<Draft> findAllDrafts(Pageable pageable);

    Optional<Draft> findExistingDraft(UUID userId, UUID productOptionId);

    void delete(Draft draft);
}
