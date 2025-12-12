package com.example.dacn2_beserver.model.summary;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalsProgress {
    private Double steps;
    private Double caloriesIn;
    private Double water;
    private Double sleep;
}
