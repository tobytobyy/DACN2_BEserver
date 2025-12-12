package com.example.dacn2_beserver.model.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;

    private String email;
    private String username;
    private String passwordHash;

    private AuthProvider provider;
    private List<String> roles;

    private UserProfile profile;
    private UserGoals goals;
    private UserSettings settings;
    private ConnectedServices connectedServices;

    private Instant createdAt;
    private Instant updatedAt;
}
