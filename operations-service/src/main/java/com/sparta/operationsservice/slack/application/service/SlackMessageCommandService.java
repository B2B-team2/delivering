package com.sparta.operationsservice.slack.application.service;

import com.sparta.operationsservice.slack.domain.core.SlackMessage;
import com.sparta.operationsservice.slack.domain.repository.SlackMessageRepository;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSearchResponse;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSendResponse;
import com.sparta.operationsservice.slack.presentation.dto.request.SlackMessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor 
public class SlackMessageCommandService {

    private final SlackMessageRepository slackMessageRepository;

    @Transactional
    public SlackMessageSendResponse createSlackMessage(SlackMessageSendRequest request) {

        SlackMessage slackMessage = SlackMessage.builder()
                .receiverSlackId(request.getReceiverSlackId())
                .receiverSlackId(request.getReceiverSlackId())
                .messageContent(request.getMessageContent())
                .referenceType(request.getReferenceType())
                .build();

        SlackMessage savedSlack = slackMessageRepository.save(slackMessage);

        return SlackMessageSendResponse.builder()
                .messageId(savedSlack.getMessageId())
                .receiverSlackId(savedSlack.getReceiverSlackId())
                .receiverUserId(savedSlack.getReceiverUserId())
                .messageContent(savedSlack.getMessageContent())
                .referenceType(savedSlack.getReferenceType())
                .status(savedSlack.getStatus())
                .sentAt(savedSlack.getSentAt())
                .build();
    }

}
