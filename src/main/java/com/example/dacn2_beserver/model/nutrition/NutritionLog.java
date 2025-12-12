package com.example.dacn2_beserver.model.nutrition;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "nutrition_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionLog {

    @Id
    private String id;

    private String userId;

    private Instant loggedAt;
    private MealType mealType;

    private List<NutritionItem> items;

    private Double totalCalories;

    private String source; // MANUAL, AI_SUGGESTED, GOOGLE_FIT
    private String note;

    private Instant createdAt;
}
