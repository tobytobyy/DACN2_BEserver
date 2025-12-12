package com.example.dacn2_beserver.model.summary;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryEmbed {
    private String text;
    private Instant generatedAt;
}
