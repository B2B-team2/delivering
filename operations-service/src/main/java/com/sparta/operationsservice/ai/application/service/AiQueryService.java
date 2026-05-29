package com.sparta.operationsservice.ai.application.service;

import com.sparta.common.dto.BusinessException;
import com.sparta.operationsservice.ai.domain.core.Ai;
import com.sparta.operationsservice.ai.domain.repository.AiRepository;
import com.sparta.operationsservice.ai.presentation.dto.response.AiCreateResponse;
import com.sparta.operationsservice.global.exception.OperationErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiQueryService {

    private final AiRepository aiHistoryRepository;

    public AiCreateResponse getAiRequest(UUID requestId) {
        Ai history = aiHistoryRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException(OperationErrorCode.REQUEST_NOT_FOUND));

        return AiCreateResponse.builder()
                .requestId(history.getRequestId())
                .userId(history.getUserId())
                .deliveryId(history.getDeliveryId())
                .aiModelName(history.getAiModelName())
                .promptText(history.getPromptText())
                .responseText(history.getResponseText())
                .status(String.valueOf(history.getStatus()))
                .errorMessage(history.getErrorMessage())
                .finalDeadlineAt(history.getFinalDeadlineAt())
                .build();
    }

    @Transactional
    public Page<AiCreateResponse> getAllAiRequests(Pageable pageable) {
        Page<Ai> historyPage = aiHistoryRepository.findAll(pageable);

        // Page 객체의 map을 사용하면 내부의 엔티티를 DTO로 손쉽게 변환 가능
        return historyPage.map(ai -> AiCreateResponse.builder()
                .requestId(ai.getRequestId())
                .userId(ai.getUserId())
                .deliveryId(ai.getDeliveryId())
                .aiModelName(ai.getAiModelName())
                .promptText(ai.getPromptText())
                .responseText(ai.getResponseText())
                .status(String.valueOf(ai.getStatus()))
                .errorMessage(ai.getErrorMessage())
                .finalDeadlineAt(ai.getFinalDeadlineAt())
                .build()
        );
    }
}