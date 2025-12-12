package com.example.dacn2_beserver.model.water;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "water_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterLog {

    @Id
    private String id;

    private String userId;

    private Instant loggedAt;
    private Integer amountMl;

    private WaterSource source;

    private Instant createdAt;
}
