package com.example.dacn2_beserver.controller;

import com.example.dacn2_beserver.dto.ai.*;
import com.example.dacn2_beserver.dto.common.ApiResponse;
import com.example.dacn2_beserver.security.AuthPrincipal;
import com.example.dacn2_beserver.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/sessions")
    public ApiResponse<ChatSessionResponse> createSession(
            @AuthenticationPrincipal AuthPrincipal principal,
            @RequestBody(required = false) CreateChatSessionRequest req
    ) {
        return ApiResponse.ok(chatService.createSession(principal.userId(), req));
    }

    @GetMapping("/sessions")
    public ApiResponse<List<ChatSessionResponse>> listSessions(
            @AuthenticationPrincipal AuthPrincipal principal
    ) {
        return ApiResponse.ok(chatService.listSessions(principal.userId()));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<ChatMessageResponse>> listMessages(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String sessionId
    ) {
        return ApiResponse.ok(chatService.listMessages(principal.userId(), sessionId));
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public ApiResponse<SendChatMessageResponse> sendMessage(
            @AuthenticationPrincipal AuthPrincipal principal,
            @PathVariable String sessionId,
            @RequestBody SendChatMessageRequest req
    ) {
        return ApiResponse.ok(chatService.sendMessage(principal.userId(), sessionId, req));
    }
}