package com.sparta.operationsservice.ai;

import com.sparta.operationsservice.ai.application.service.AiQueryService;
import com.sparta.operationsservice.ai.application.service.AiService;
import com.sparta.operationsservice.ai.domain.core.Ai;
import com.sparta.operationsservice.ai.domain.core.AiRequestStatus;
import com.sparta.operationsservice.ai.domain.repository.AiRepository;
import com.sparta.operationsservice.ai.presentation.dto.requset.AiRequest;
import com.sparta.operationsservice.ai.presentation.dto.response.AiCreateResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiRepository aiRepository;

    @Mock private WebClient webClient;
    
    @InjectMocks
    private AiService aiService;
    @InjectMocks
    private AiQueryService aiQueryService;

    @Test
    @DisplayName("AI 분석 결과 단건 조회 성공")
    void getAiRequest_Success() {
        // Given
        UUID requestId = UUID.randomUUID();
        Ai ai = Ai.builder()
                .requestId(requestId)
                .status(AiRequestStatus.SUCCESS)
                .finalDeadlineAt(LocalDateTime.now())
                .build();
        
        given(aiRepository.findById(requestId)).willReturn(Optional.of(ai));

        // When
        AiCreateResponse response = aiQueryService.getAiRequest(requestId);

        // Then
        assertThat(response.getRequestId()).isEqualTo(requestId);
    }

    @Test
    @DisplayName("AI 분석 결과 전체 조회(페이징) 성공")
    void getAllAiRequests_Success() {
        // Given
        Ai ai = Ai.builder().requestId(UUID.randomUUID()).status(AiRequestStatus.SUCCESS).build();
        given(aiRepository.findAll(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(ai)));

        // When
        Page<AiCreateResponse> result = aiQueryService.getAllAiRequests(PageRequest.of(0, 10));

        // Then
        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}