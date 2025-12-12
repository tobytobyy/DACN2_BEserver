package com.example.dacn2_beserver.model.nutrition;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionItem {
    private String name;
    private Double quantity;
    private Double calories;
}

