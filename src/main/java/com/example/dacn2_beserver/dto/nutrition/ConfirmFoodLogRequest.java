package com.example.dacn2_beserver.dto.nutrition;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmFoodLogRequest {
    private String foodCode;      // required, equals AI label and FoodItem.code
    private Double confidence;    // optional (0..1)
}