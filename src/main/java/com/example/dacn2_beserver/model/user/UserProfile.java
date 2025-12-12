package com.example.dacn2_beserver.model.user;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {
    private String fullName;
    private Gender gender;
    private LocalDate birthDate;
    private Double heightCm;
    private Double weightKg;
    private String avatarUrl;
}
