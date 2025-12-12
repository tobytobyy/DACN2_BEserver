package com.example.dacn2_beserver.model.auth;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "refresh_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    private String id;

    private String userId;
    private String token;

    private DeviceInfo deviceInfo;

    private Instant expiresAt;
    private Instant createdAt;
    private boolean revoked;
    private Instant revokedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceInfo {
        private String platform;   // ANDROID, IOS, WEB
        private String model;
        private String appVersion;
    }
}
