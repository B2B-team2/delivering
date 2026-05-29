package com.sparta.operationsservice.slack.infrastructure.repository;

import com.sparta.operationsservice.slack.domain.core.SlackMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SlackMessageJpaRepository extends JpaRepository<SlackMessage, UUID> {

    Page<SlackMessage> findAll(Pageable pageable);
}
