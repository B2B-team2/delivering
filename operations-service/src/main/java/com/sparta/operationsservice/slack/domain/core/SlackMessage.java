package com.sparta.operationsservice.slack.domain.core;

import com.sparta.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_slack_messages")
@Getter
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlackMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID messageId;

    private UUID receiverUserId;

    @Column(nullable = false, length = 36)
    private String receiverSlackId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @Column(length = 30)
    private String referenceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SlackMessageStatus status;

    private LocalDateTime sentAt;

    @Builder
    public SlackMessage(UUID receiverUserId, String receiverSlackId, String messageContent, String referenceType) {
        this.receiverUserId = receiverUserId;
        this.receiverSlackId = receiverSlackId;
        this.messageContent = messageContent;
        this.referenceType = referenceType;
        this.status = SlackMessageStatus.PENDING;
        this.sentAt = LocalDateTime.now();
    }

    public void updateContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public void updateStatus(SlackMessageStatus status) {
        this.status = status;
        if (status == SlackMessageStatus.SENT) {
            this.sentAt = LocalDateTime.now();
        }
    }

}