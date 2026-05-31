package com.sparta.operationsservice.ai.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.operationsservice.ai.application.service.AiQueryService;
import com.sparta.operationsservice.ai.application.service.AiService;
import com.sparta.operationsservice.ai.presentation.dto.requset.AiRequest;
import com.sparta.operationsservice.ai.presentation.dto.response.AiCreateResponse;
import com.sparta.operationsservice.ai.presentation.dto.response.AiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    private final AiQueryService aiQueryService;

    @PostMapping("/api/v1/ai/generate")
    public AiResponse generateAiDescription(@RequestBody AiRequest request) {
        AiResponse response = aiService.generateDescription(request);
        return response;
    }

    @GetMapping("/requests/{request_id}")
    public ResponseEntity<ApiResponse<AiCreateResponse>> getAiRequest(@PathVariable("request_id") UUID requestId) {
        AiCreateResponse response = aiQueryService.getAiRequest(requestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @GetMapping("/requests")
    public ResponseEntity<ApiResponse<Page<AiCreateResponse>>> getAllAiRequests(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(aiQueryService.getAllAiRequests(pageable)));
    }
}

