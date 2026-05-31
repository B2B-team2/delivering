package com.sparta.operationsservice.slack;

import com.sparta.common.dto.BusinessException;
import com.sparta.operationsservice.global.application.service.security.SecurityUtils;
import com.sparta.operationsservice.global.exception.OperationErrorCode;
import com.sparta.operationsservice.slack.application.service.SlackMessageCommandService;
import com.sparta.operationsservice.slack.application.service.SlackMessageQueryService;
import com.sparta.operationsservice.slack.domain.core.SlackMessage;
import com.sparta.operationsservice.slack.domain.repository.SlackMessageRepository;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSearchDetailResponse;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSearchResponse;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSendResponse;
import com.sparta.operationsservice.slack.presentation.dto.request.SlackMessageSendRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SlackMessageCommandServiceTest {

    @Mock
    private SlackMessageRepository slackMessageRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private SlackMessageCommandService slackMessageCommandService;

    @InjectMocks
    private SlackMessageQueryService slackMessageQueryService;

    @Test
    @DisplayName("슬랙 메시지 저장 및 응답 생성 성공")
    void createSlackMessage_Success() {
        // Given
        SlackMessageSendRequest request = SlackMessageSendRequest.builder()
                .receiverSlackId("slack_id_123")
                .messageContent("테스트 메시지")
                .referenceType("DELIVERY_NOTIFICATION")
                .build();

        // 저장된 메시지 시뮬레이션 (Id 할당)
        SlackMessage savedSlackMessage = SlackMessage.builder()
                .receiverSlackId(request.getReceiverSlackId())
                .messageContent(request.getMessageContent())
                .referenceType(request.getReferenceType())
                .build();
        
        // Reflection 등을 통해 messageId 주입이 어렵다면 Mockito의 answer를 쓰거나,
        // 도메인 생성 시 ID를 생성하는 로직을 가정합니다.
        given(slackMessageRepository.save(any(SlackMessage.class))).willReturn(savedSlackMessage);

        // When
        SlackMessageSendResponse response = slackMessageCommandService.createSlackMessage(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getMessageContent()).isEqualTo("테스트 메시지");
        assertThat(response.getReceiverSlackId()).isEqualTo("slack_id_123");
        
        verify(slackMessageRepository).save(any(SlackMessage.class));
    }

    @Test
    @DisplayName("전체 슬랙 메시지 조회 성공")
    void getAllSlackMessages_Success() {
        // Given
        given(securityUtils.isMaster()).willReturn(true);
        SlackMessage message = SlackMessage.builder().build();
        given(slackMessageRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of(message)));

        // When
        Page<SlackMessageSearchResponse.SlackMessageDto> result = slackMessageQueryService.getAllSlackMessages(Pageable.unpaged());

        // Then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("권한 없는 사용자가 메시지 조회 시 예외 발생")
    void getAllSlackMessages_Unauthorized_ThrowsException() {
        // Given
        given(securityUtils.isMaster()).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> slackMessageQueryService.getAllSlackMessages(Pageable.unpaged()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", OperationErrorCode.UNAUTHORIZED_ACCESS);
    }

    @Test
    @DisplayName("슬랙 메시지 상세 조회 성공")
    void getSlackMessageDetails_Success() {
        // Given
        UUID messageId = UUID.randomUUID();
        SlackMessage message = SlackMessage.builder().build();

        given(securityUtils.isMaster()).willReturn(true);
        given(slackMessageRepository.findById(messageId)).willReturn(Optional.of(message));

        // When
        SlackMessageSearchDetailResponse response = slackMessageQueryService.getSlackMessageDetails(messageId);

        // Then
        assertThat(response).isNotNull();
    }
}