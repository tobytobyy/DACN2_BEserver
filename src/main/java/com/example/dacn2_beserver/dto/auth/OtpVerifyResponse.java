package com.example.dacn2_beserver.dto.auth;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyResponse {
    private String userId;
    private String sessionId;

    private String accessToken;
    private String refreshToken;
    private Long expiresInSeconds;

    // ✅ gợi ý UI nếu email này đang có Google account
    private Boolean linkSuggested;
    private String message;
}