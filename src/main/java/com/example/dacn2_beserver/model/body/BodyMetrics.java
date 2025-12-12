package com.example.dacn2_beserver.model.body;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "body_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodyMetrics {

    @Id
    private String id;

    private String userId;

    private Instant loggedAt;

    private Double weightKg;
    private Double bodyFatPercent;
    private Double bmi;
    private Double waistCm;

    private String source; // MANUAL, SMART_SCALE, ...

    private Instant createdAt;
}