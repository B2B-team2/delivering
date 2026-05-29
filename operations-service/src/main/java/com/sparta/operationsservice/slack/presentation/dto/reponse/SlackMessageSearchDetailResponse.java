package com.sparta.operationsservice.slack.presentation.dto.reponse;

import com.sparta.operationsservice.slack.domain.core.SlackMessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackMessageSearchDetailResponse {
    private UUID messageId;
    private UUID receiverUserId;
    private String receiverSlackId;
    private String messageContent;
    private String referenceType;
    private SlackMessageStatus status;
    private LocalDateTime sendAt;
}
