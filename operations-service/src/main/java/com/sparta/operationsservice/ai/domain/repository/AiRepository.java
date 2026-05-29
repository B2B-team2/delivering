package com.sparta.operationsservice.ai.domain.repository;

import com.sparta.operationsservice.ai.domain.core.Ai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AiRepository {
    Ai save(Ai ai);
    Optional<Ai> findById(UUID requestId);
    Page<Ai> findAll(Pageable pageable);
}
