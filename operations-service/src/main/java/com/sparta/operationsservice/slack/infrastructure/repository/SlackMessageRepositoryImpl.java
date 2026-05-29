package com.sparta.operationsservice.slack.infrastructure.repository;

import com.sparta.operationsservice.slack.domain.core.SlackMessage;
import com.sparta.operationsservice.slack.domain.repository.SlackMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SlackMessageRepositoryImpl implements SlackMessageRepository {

    private final SlackMessageJpaRepository slackMessageJpaRepository;

    @Override
    public Optional<SlackMessage> findById(UUID slackMessageId) { return slackMessageJpaRepository.findById(slackMessageId);}

    @Override
    public SlackMessage save(SlackMessage slackMessage) { return slackMessageJpaRepository.save(slackMessage);}

    @Override
    public Page<SlackMessage> findAll(Pageable pageable) {return slackMessageJpaRepository.findAll(pageable);}
}
