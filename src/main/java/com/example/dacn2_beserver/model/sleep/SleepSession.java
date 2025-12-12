package com.example.dacn2_beserver.model.sleep;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "sleep_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepSession {

    @Id
    private String id;

    private String userId;
    private String deviceId;
    private String source; // SMARTWATCH, PHONE, GOOGLE_FIT, ...

    private Instant startTime;
    private Instant endTime;
    private Integer durationMinutes;

    private Integer qualityScore;
    private Integer awakenings;

    private SleepStages stages;

    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SleepStages {
        private Integer awakeMinutes;
        private Integer lightMinutes;
        private Integer deepMinutes;
        private Integer remMinutes;
    }
}