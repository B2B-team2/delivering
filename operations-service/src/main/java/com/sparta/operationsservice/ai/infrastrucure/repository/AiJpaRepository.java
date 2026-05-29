package com.sparta.operationsservice.ai.infrastrucure.repository;

import com.sparta.operationsservice.ai.domain.core.Ai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AiJpaRepository extends JpaRepository<Ai, UUID> {
    Page<Ai> findAll(Pageable pageable);

}
