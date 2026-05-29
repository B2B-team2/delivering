package com.sparta.operationsservice.ai.domain.core;

import com.sparta.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_ai_requests", schema = "operation-db")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ai extends BaseEntity {

    @Id
    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "ai_model_name", nullable = false, length = 100)
    private String aiModelName;

    @Column(name = "prompt_text", nullable = false, columnDefinition = "TEXT")
    private String promptText;

    @Column(name = "response_text", columnDefinition = "TEXT")
    private String responseText;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AiRequestStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "final_deadline_at")
    private LocalDateTime finalDeadlineAt;

}