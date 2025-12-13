package com.example.dacn2_beserver.dto.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendChatMessageRequest {
    @NotBlank
    private String content;

    // optional metadata
    private Map<String, Object> meta;
}