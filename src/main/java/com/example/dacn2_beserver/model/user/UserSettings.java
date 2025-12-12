package com.example.dacn2_beserver.model.user;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings {

    private UnitSystem unitSystem;
    private String language;
    private NotificationSettings notifications;
}