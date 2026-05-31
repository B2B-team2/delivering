package com.sparta.operationsservice.slack.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.operationsservice.global.application.service.security.SecurityUtils;
import com.sparta.operationsservice.global.exception.OperationErrorCode;
import com.sparta.operationsservice.slack.domain.core.SlackMessage;
import com.sparta.operationsservice.slack.domain.repository.SlackMessageRepository;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSearchDetailResponse;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSearchResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor 
public class SlackMessageQueryService {

    private final SlackMessageRepository slackMessageRepository;

    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public Page<SlackMessageSearchResponse.SlackMessageDto> getAllSlackMessages(Pageable pageable) {

        if (!securityUtils.isMaster()) {
            throw new BusinessException(OperationErrorCode.UNAUTHORIZED_ACCESS);
        }
        Page<SlackMessage> slackMessages = slackMessageRepository.findAll(pageable);

        return slackMessages.map(slackMessage -> SlackMessageSearchResponse.SlackMessageDto.builder()
                .messageId(slackMessage.getMessageId())
                .receiverUserId(slackMessage.getReceiverUserId())
                .receiverSlackId(slackMessage.getReceiverSlackId())
                .messageContent(String.valueOf(slackMessage.getMessageContent()))
                .referenceType(slackMessage.getReferenceType())
                .status(slackMessage.getStatus())
                .sentAt(slackMessage.getSentAt())
                .build()
        );
    }

    @Transactional
    public SlackMessageSearchDetailResponse getSlackMessageDetails(UUID messageId) {

        if (!securityUtils.isMaster()) {
            throw new BusinessException(OperationErrorCode.UNAUTHORIZED_ACCESS);
        }

        SlackMessage slackMessage = slackMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(OperationErrorCode.SLACK_NOT_FOUND));

        return SlackMessageSearchDetailResponse.builder()
                .messageId(slackMessage.getMessageId())
                .receiverUserId(slackMessage.getReceiverUserId())
                .receiverSlackId(slackMessage.getReceiverSlackId())
                .messageContent(String.valueOf(slackMessage.getMessageContent()))
                .referenceType(slackMessage.getReferenceType())
                .status(slackMessage.getStatus())
                .sendAt(slackMessage.getSentAt())
                .build();
    }
}
