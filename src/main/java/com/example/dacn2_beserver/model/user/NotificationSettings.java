package com.example.dacn2_beserver.model.user;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettings {
    private boolean enabled;
    private boolean remindDrinkWater;
    private boolean remindSleep;
}
