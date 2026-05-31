package com.sparta.operationsservice.slack.domain.repository;

import com.sparta.operationsservice.slack.domain.core.SlackMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SlackMessageRepository {

    Optional<SlackMessage> findById(UUID slackMessageId);

    SlackMessage save(SlackMessage slackMessage);

    Page<SlackMessage> findAll(Pageable pageable);
}
