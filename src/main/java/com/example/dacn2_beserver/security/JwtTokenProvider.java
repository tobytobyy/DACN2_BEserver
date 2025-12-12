package com.example.dacn2_beserver.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    @Value("${JWT_SECRET}")
    private String secret;

    @Value("${JWT_EXPIRATION}")
    private long validityInMs;

    private Key key;

    @PostConstruct
    public void init() {
        // HS256 key
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String userId, String username, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(validityInMs);

        JwtBuilder builder = Jwts.builder()
                .setSubject(userId) // chủ thể: userId
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiry))
                .claim("username", username)
                .signWith(key, SignatureAlgorithm.HS256);

        if (extraClaims != null) {
            extraClaims.forEach(builder::claim);
        }

        return builder.compact();
    }

    public String generateToken(String userId, String username) {
        return generateToken(userId, username, null);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUserId(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    public String getUsername(String token) {
        return parseClaims(token).getBody().get("username", String.class);
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
