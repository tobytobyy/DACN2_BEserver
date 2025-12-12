package com.example.dacn2_beserver.model.summary;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyHeartSummary {
    private Integer restingBpm;
    private Integer avgBpm;
    private Integer maxBpm;
}
