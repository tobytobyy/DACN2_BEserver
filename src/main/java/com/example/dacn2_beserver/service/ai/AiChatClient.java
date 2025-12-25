package com.example.dacn2_beserver.service.ai;

import com.example.dacn2_beserver.dto.ai.AiChatRequest;
import com.example.dacn2_beserver.dto.ai.AiChatResponse;
import com.example.dacn2_beserver.exception.ApiException;
import com.example.dacn2_beserver.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class AiChatClient {

    private final RestClient aiRestClient;

    @Value("${ai.chat.path}")
    private String chatPath;

    @Value("${ai.internal-token:}")
    private String internalToken;

    public AiChatResponse chat(AiChatRequest req) {
        try {
            var spec = aiRestClient.post()
                    .uri(chatPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON);

            if (internalToken != null && !internalToken.isBlank()) {
                spec = spec.header("X-Internal-Token", internalToken);
            }

            return spec.body(req).retrieve().body(AiChatResponse.class);

        } catch (Exception e) {
            throw new ApiException(ErrorCode.AI_SERVICE_ERROR, "Failed to call AI chat: " + e.getMessage());
        }
    }
}