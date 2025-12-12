package com.example.dacn2_beserver.model.ai;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "ai_insights")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInsight {

    @Id
    private String id;

    private String userId;

    private AiInsightType type;
    private String date; // optional yyyy-MM-dd

    private String text;
    private String html;

    private Map<String, Object> metadata;

    private Instant createdAt;
}
