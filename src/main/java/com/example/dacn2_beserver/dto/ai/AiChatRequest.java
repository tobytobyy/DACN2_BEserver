package com.example.dacn2_beserver.dto.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiChatRequest {

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user_id")
    private String userId;

    // text
    @JsonProperty("message")
    private String message;

    // optional image
    @JsonProperty("image_url")
    private String imageUrl;

    // optional: history/context (AI có thể ignore)
    @JsonProperty("history")
    private List<Map<String, Object>> history;

    @JsonProperty("user_context")
    private Map<String, Object> userContext;
}