package com.example.dacn2_beserver.model.heart;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "heart_rate")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartRate {
    @Id
    private String id;

    private String userId;
    private String deviceId;

    private Instant timestamp;
    private Integer bpm;

    private HeartContext context;

    private Instant createdAt;
}

