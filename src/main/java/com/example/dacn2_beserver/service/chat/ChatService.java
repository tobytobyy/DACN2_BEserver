package com.example.dacn2_beserver.service.chat;

import com.example.dacn2_beserver.dto.ai.*;
import com.example.dacn2_beserver.exception.ApiException;
import com.example.dacn2_beserver.exception.ErrorCode;
import com.example.dacn2_beserver.model.ai.ChatMessage;
import com.example.dacn2_beserver.model.ai.ChatSession;
import com.example.dacn2_beserver.model.enums.ChatRole;
import com.example.dacn2_beserver.repository.ChatMessageRepository;
import com.example.dacn2_beserver.repository.ChatSessionRepository;
import com.example.dacn2_beserver.service.ai.AiChatClient;
import com.example.dacn2_beserver.service.storage.ChatMediaS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;

    private final ChatMediaS3Service chatMediaS3Service;
    private final AiChatClient aiChatClient;

    public ChatSessionResponse createSession(String userId, CreateChatSessionRequest req) {
        String title = (req != null && req.getTitle() != null) ? req.getTitle().trim() : null;
        if (title == null || title.isBlank()) title = "New chat";

        ChatSession s = ChatSession.builder()
                .userId(userId)
                .title(title)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .contextSnapshot(req != null ? req.getContextSnapshot() : null)
                .build();

        s = chatSessionRepository.save(s);
        return toSessionResponse(s);
    }

    public List<ChatSessionResponse> listSessions(String userId) {
        return chatSessionRepository.findTop50ByUserIdOrderByUpdatedAtDesc(userId)
                .stream().map(this::toSessionResponse).toList();
    }

    public List<ChatMessageResponse> listMessages(String userId, String sessionId) {
        ChatSession s = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Session not found"));

        return chatMessageRepository.findTop100BySessionIdOrderByCreatedAtAsc(s.getId())
                .stream().map(this::toMessageResponse).toList();
    }

    public SendChatMessageResponse sendMessage(String userId, String sessionId, SendChatMessageRequest req) {
        ChatSession session = chatSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Session not found"));

        String content = req != null ? req.getContent() : null;
        String imageObjectKey = req != null ? req.getImageObjectKey() : null;

        boolean hasText = content != null && !content.trim().isBlank();
        boolean hasImage = imageObjectKey != null && !imageObjectKey.trim().isBlank();

        if (!hasText && !hasImage) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, "Either content or imageObjectKey is required");
        }

        Map<String, Object> userMeta = new HashMap<>();
        if (req != null && req.getMeta() != null) userMeta.putAll(req.getMeta());

        String imageUrl = null;
        if (hasImage) {
            chatMediaS3Service.assertOwnedByUser(userId, imageObjectKey);
            imageUrl = chatMediaS3Service.presignGetUrl(imageObjectKey);

            userMeta.put("image", Map.of(
                    "objectKey", imageObjectKey,
                    "url", imageUrl
            ));
        }

        ChatMessage userMsg = ChatMessage.builder()
                .sessionId(session.getId())
                .role(ChatRole.USER)
                .content(hasText ? content.trim() : null)
                .meta(userMeta.isEmpty() ? null : userMeta)
                .createdAt(Instant.now())
                .build();
        userMsg = chatMessageRepository.save(userMsg);

        // history (optional) - lấy 100 gần nhất (đã sort asc)
        List<Map<String, Object>> history = chatMessageRepository.findTop100BySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(m -> {
                    Map<String, Object> mm = new HashMap<>();
                    mm.put("role", m.getRole());
                    mm.put("content", m.getContent());
                    if (m.getMeta() != null && m.getMeta().get("image") instanceof Map<?, ?> im) {
                        Object url = ((Map<?, ?>) im).get("url");
                        if (url != null) mm.put("image_url", url);
                    }
                    return mm;
                }).toList();

        AiChatRequest aiReq = AiChatRequest.builder()
                .sessionId(session.getId())
                .userId(userId)
                .message(hasText ? content.trim() : null)
                .imageUrl(imageUrl)
                .history(history)
                .userContext(session.getContextSnapshot())
                .build();

        AiChatResponse aiRes = aiChatClient.chat(aiReq);

        Map<String, Object> assistantMeta = new HashMap<>();
        if (aiRes != null && aiRes.getSuggestedActions() != null) {
            assistantMeta.put("suggested_actions", aiRes.getSuggestedActions());
        }
        if (aiRes != null && aiRes.getMeta() != null) {
            assistantMeta.put("ai_meta", aiRes.getMeta());
        }

        ChatMessage assistantMsg = ChatMessage.builder()
                .sessionId(session.getId())
                .role(ChatRole.ASSISTANT)
                .content(aiRes != null ? aiRes.getContent() : null)
                .meta(assistantMeta.isEmpty() ? null : assistantMeta)
                .createdAt(Instant.now())
                .build();
        assistantMsg = chatMessageRepository.save(assistantMsg);

        // bump session updatedAt
        session.setUpdatedAt(Instant.now());
        chatSessionRepository.save(session);

        return SendChatMessageResponse.builder()
                .userMessage(toMessageResponse(userMsg))
                .assistantMessage(toMessageResponse(assistantMsg))
                .build();
    }

    private ChatSessionResponse toSessionResponse(ChatSession s) {
        return ChatSessionResponse.builder()
                .id(s.getId())
                .title(s.getTitle())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .contextSnapshot(s.getContextSnapshot())
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage m) {
        return ChatMessageResponse.builder()
                .id(m.getId())
                .sessionId(m.getSessionId())
                .role(m.getRole())
                .content(m.getContent())
                .meta(m.getMeta())
                .createdAt(m.getCreatedAt())
                .build();
    }
}