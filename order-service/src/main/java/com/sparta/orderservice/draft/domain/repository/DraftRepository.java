package com.sparta.orderservice.draft.domain.repository;

import com.sparta.orderservice.draft.domain.core.Draft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface DraftRepository {

    Draft save(Draft draft);

    Optional<Draft> findDraftById(UUID draftId);

    Page<Draft> findAllByUserId(UUID userId, Pageable pageable);

    // soft-deleted 포함 조회 (upsert 패턴: 같은 상품 옵션 재담기 시 복원)
    Optional<Draft> findByUserIdAndProductOptionId(UUID userId, UUID productOptionId);

    void delete(Draft draft);
}
