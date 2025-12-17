package com.example.dacn2_beserver.dto.ai;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiFoodPredictResponse {
    private String label;
    private Double confidence;
}