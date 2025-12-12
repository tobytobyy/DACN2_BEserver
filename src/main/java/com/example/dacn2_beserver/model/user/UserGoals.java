package com.example.dacn2_beserver.model.user;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGoals {
    private Integer dailySteps;
    private Integer dailyCaloriesIn;
    private Integer dailyCaloriesOut;
    private Integer dailyWaterMl;
}
