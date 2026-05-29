package com.sparta.operationsservice.slack.presentation.dto.reponse;

import com.sparta.operationsservice.slack.domain.core.SlackMessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SlackMessageSearchResponse {
    private List<SlackMessageDto> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private String sort;

    @Getter
    @Builder
    public static class SlackMessageDto {
        private UUID messageId;
        private UUID receiverUserId;
        private String receiverSlackId;
        private String messageContent;
        private String referenceType;
        private SlackMessageStatus status;
        private LocalDateTime sentAt;
    }
}
