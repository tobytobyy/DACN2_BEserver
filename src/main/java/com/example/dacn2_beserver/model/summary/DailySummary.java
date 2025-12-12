package com.example.dacn2_beserver.model.summary;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "daily_summaries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummary {

    @Id
    private String id;

    private String userId;
    private String date; // yyyy-MM-dd

    private Integer steps;
    private Double distanceMeters;
    private Double caloriesIn;
    private Double caloriesOut;
    private Integer waterMl;

    private DailySleepSummary sleep;
    private DailyHeartSummary heart;
    private GoalsProgress goalsProgress;

    private AiSummaryEmbed aiSummary;

    private Instant createdAt;
    private Instant updatedAt;
}
