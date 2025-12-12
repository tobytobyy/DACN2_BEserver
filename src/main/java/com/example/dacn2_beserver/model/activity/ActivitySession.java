package com.example.dacn2_beserver.model.activity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "activity_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivitySession {

    @Id
    private String id;

    private String userId;
    private String deviceId;
    private ActivitySource source;

    private ActivityType type;

    private Instant startTime;
    private Instant endTime;
    private Long durationSec;

    private Integer steps;
    private Double distanceMeters;
    private Double caloriesOut;
    private Integer avgHeartRate;
    private Integer maxHeartRate;

    private Route route;

    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Route {
        private String type; // "LineString"
        private List<List<Double>> coordinates; // [ [lon, lat], ... ]
        private Integer samplingIntervalSec;
    }
}
