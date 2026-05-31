package com.sparta.operationsservice.ai.application.service;

import com.sparta.operationsservice.ai.domain.core.Ai;
import com.sparta.operationsservice.ai.domain.core.AiRequestStatus;
import com.sparta.operationsservice.ai.domain.repository.AiRepository;
import com.sparta.operationsservice.ai.presentation.dto.requset.AiRequest;
import com.sparta.operationsservice.ai.presentation.dto.response.AiResponse;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {

    private final WebClient webClient;

    private final AiRepository aiRepository;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    @Value("${GEMINI_URL}")
    private String geminiUrl;

    public AiResponse generateDescription(AiRequest request) {

        try {
            String prompt = "출발 허브: " + request.getFromHubName() +
                    ", 도착지 상세 주소: " + request.getDeliveryAddress() + ". " +
                    request.getPromptText() +
                    "계산된 배송 완료 예상 시한을 딱 'yyyy-MM-dd HH:mm' 형식의 날짜로만 답변해.\\n" +
                    "절대 다른 설명, 숫자, 경로 계산 과정은 출력하지 말고, 오직 '2026-05-30 14:00' 같은 날짜 데이터만 출력해.";

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt))))
            );
            String fullUrl = geminiUrl + "?key=" + geminiApiKey;
            System.out.println("Calling URL: " + fullUrl);
            Map<String, Object> response = webClient.post()
                    .uri(geminiUrl + "?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                    })
                    .block();

            String aiText = parseGeminiResponse(response).trim();
            LocalDateTime deadline = parseDateTime(aiText);

            Ai history = Ai.builder()
                    .requestId(UUID.randomUUID())
                    .userId(request.getUserId())
                    .deliveryId(request.getDeliveryId())
                    .aiModelName(request.getAiModelName())
                    .promptText(request.getPromptText())
                    .responseText(aiText)
                    .status(AiRequestStatus.SUCCESS)
                    .errorMessage(null)
                    .finalDeadlineAt(deadline)
                    .build();

            aiRepository.save(history);

            return AiResponse.builder()
                    .requestId(UUID.randomUUID())
                    .userId(request.getUserId())
                    .deliveryId(request.getDeliveryId())
                    .aiModelName(request.getAiModelName())
                    .promptText(request.getPromptText())
                    .responseText(aiText)
                    .status("SUCCESS")
                    .finalDeadlineAt(deadline)
                    .build();

        } catch (Exception e) {
            Ai history = Ai.builder()
                    .requestId(UUID.randomUUID())
                    .userId(request.getUserId())
                    .deliveryId(request.getDeliveryId())
                    .aiModelName(request.getAiModelName())
                    .promptText(request.getPromptText())
                    .responseText("분석 실패")
                    .status(AiRequestStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .finalDeadlineAt(null)
                    .build();

            aiRepository.save(history);

            return AiResponse.builder()
                    .requestId(history.getRequestId())
                    .userId(history.getUserId())
                    .deliveryId(history.getDeliveryId())
                    .aiModelName(history.getAiModelName())
                    .promptText(history.getPromptText())
                    .responseText(history.getResponseText())
                    .status("FAILED")
                    .errorMessage(history.getErrorMessage())
                    .finalDeadlineAt(history.getFinalDeadlineAt())
                    .build();
        }

    }

    private String parseGeminiResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return (String) parts.get(0).get("text");

        } catch (Exception e) {
            return "응답 추출 실패: " + e.getMessage();
        }
    }
    private LocalDateTime parseDateTime(String aiText) {
        try {
            String cleaned = aiText.replaceAll("[^0-9-: ]", "").trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return LocalDateTime.parse(cleaned, formatter);
        } catch (Exception e) {
            return LocalDateTime.now().plusHours(2);
        }
    }
}
