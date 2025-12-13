package com.example.dacn2_beserver.model.health;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document("foods")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodItem {

    @Id
    private String id;

    @Indexed
    private String name;

    private String brand;
    private List<String> tags;

    private Double calories;

    private Double servingSizeG;
    private String servingLabel;

    @Builder.Default
    private Instant createdAt = Instant.now();
    @Builder.Default
    private Instant updatedAt = Instant.now();
}