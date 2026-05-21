package com.sparta.orderservice.draft.infrastructure.repository;

import com.sparta.orderservice.draft.domain.core.Draft;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DraftJpaRepository extends JpaRepository<Draft, UUID> {

    // soft-deleted 제외 단건 조회
    Optional<Draft> findByDraftIdAndDeletedAtIsNull(UUID draftId);

    // soft-deleted 제외 목록 조회 (페이징) - 업체담당자용
    Page<Draft> findAllByUserIdAndDeletedAtIsNull(UUID userId, Pageable pageable);

    // soft-deleted 제외 전체 조회 (페이징) - 마스터용
    Page<Draft> findAllByDeletedAtIsNull(Pageable pageable);

    // soft-deleted 포함 조회 (upsert 패턴: 같은 상품 옵션 재담기 시 복원용)
    Optional<Draft> findByUserIdAndProductOptionId(UUID userId, UUID productOptionId);
}
