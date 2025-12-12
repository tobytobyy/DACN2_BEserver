package com.example.dacn2_beserver.model.auth;
import com.example.dacn2_beserver.model.user.AuthProvider;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "auth_identities")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthIdentity {
    @Id
    private String id;

    private String userId;

    private AuthProvider provider;
    private String providerUserId;

    private String email;
    private String passwordHash; // null nếu provider != LOCAL

    private Instant createdAt;
    private Instant updatedAt;
}
