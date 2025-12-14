package com.example.dacn2_beserver.service.auth;

import com.example.dacn2_beserver.model.auth.Session;
import com.example.dacn2_beserver.model.enums.DevicePlatform;
import com.example.dacn2_beserver.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final SecureRandom RNG = new SecureRandom();
    private static final long REFRESH_TTL_SECONDS = 30L * 24 * 60 * 60; // 30 ngày

    private final SessionRepository sessionRepository;

    public SessionIssueResult createSession(String userId, String deviceId, DevicePlatform platform) {
        String refreshPlain = randomToken();
        String refreshHash = OtpCrypto.sha256(refreshPlain);

        Session s = Session.builder()
                .userId(userId)
                .deviceId(deviceId)
                .platform(platform)
                .refreshTokenHash(refreshHash)
                .expiresAt(Instant.now().plusSeconds(REFRESH_TTL_SECONDS))
                .build();

        s = sessionRepository.save(s);
        return new SessionIssueResult(s, refreshPlain);
    }

    private String randomToken() {
        byte[] buf = new byte[32];
        RNG.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    public record SessionIssueResult(Session session, String refreshTokenPlain) {
    }
}