package com.sparta.orderservice.draft.domain.repository;

import com.sparta.orderservice.draft.domain.core.Draft;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DraftRepository {

    Draft save(Draft draft);

    Optional<Draft> findDraftById(UUID draftId);

    List<Draft> findAllByUserId(UUID userId);

    void delete(Draft draft);
}
