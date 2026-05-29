package com.sparta.operationsservice.slack.presentation.controller;

import com.sparta.common.dto.ApiResponse;
import com.sparta.operationsservice.slack.application.service.SlackMessageCommandService;
import com.sparta.operationsservice.slack.application.service.SlackMessageQueryService;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSearchDetailResponse;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSearchResponse;
import com.sparta.operationsservice.slack.presentation.dto.reponse.SlackMessageSendResponse;
import com.sparta.operationsservice.slack.presentation.dto.request.SlackMessageSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/slack")
public class SlackMessageController {

    private final SlackMessageCommandService slackMessageCommandService;
    private final SlackMessageQueryService  slackMessageQueryService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('MASTER', 'HUB_MANAGER', 'HUB_DELIVERY_MANAGER', 'COMPANY_DELIVERY_MANAGER')")
    public ResponseEntity<ApiResponse<SlackMessageSendResponse>> sendMessage(
            @RequestBody SlackMessageSendRequest request) {
        SlackMessageSendResponse response = slackMessageCommandService.createSlackMessage(request);
        return ResponseEntity.ok(ApiResponse.created(response));
    }

    @GetMapping("/messages")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<ApiResponse<Page<SlackMessageSearchResponse.SlackMessageDto>>> getAllSlackMessages(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SlackMessageSearchResponse.SlackMessageDto> response = slackMessageQueryService.getAllSlackMessages(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/send/{message_id}")
    @PreAuthorize("hasAnyRole('MASTER')")
    public ResponseEntity<ApiResponse<SlackMessageSearchDetailResponse>> getSlackMessage(
            @PathVariable("message_id") UUID message_id) {
        SlackMessageSearchDetailResponse response = slackMessageQueryService.getSlackMessageDetails(message_id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
