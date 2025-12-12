package com.example.dacn2_beserver.model.summary;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySleepSummary {
    private Integer durationMinutes;
    private Integer qualityScore;
}
