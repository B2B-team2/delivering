package com.sparta.operationsservice.slack.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SlackMessageSendRequest {

    @NotBlank(message = "받은 사람 아이디는 필수입니다.")
    private UUID receiverUserId;

    @NotNull(message = "받은 사람 슬랙는 필수입니다.")
    private String receiverSlackId;

    @NotNull(message = "받은 사람 슬랙는 필수입니다.")
    private String messageContent;

    @NotBlank(message = "받은 사람 슬랙는 필수입니다.")
    private String referenceType;
}
