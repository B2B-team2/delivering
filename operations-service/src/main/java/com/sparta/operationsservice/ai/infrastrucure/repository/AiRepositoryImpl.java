package com.sparta.operationsservice.ai.infrastrucure.repository;


import com.sparta.operationsservice.ai.domain.core.Ai;
import com.sparta.operationsservice.ai.domain.repository.AiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AiRepositoryImpl implements AiRepository {
    private final AiJpaRepository aiJpaRepository;

    @Override
    public Ai save(Ai aiRequest) {
        return aiJpaRepository.save(aiRequest);
    }

    @Override
    public Optional<Ai> findById(UUID requestId) {
        return aiJpaRepository.findById(requestId);
    }

    @Override
    public Page<Ai> findAll(Pageable pageable) { return aiJpaRepository.findAll(pageable);}
}
