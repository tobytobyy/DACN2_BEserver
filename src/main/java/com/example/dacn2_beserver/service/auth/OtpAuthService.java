package com.example.dacn2_beserver.service.auth;

import com.example.dacn2_beserver.dto.auth.OtpRequestCreateRequest;
import com.example.dacn2_beserver.dto.auth.OtpRequestCreateResponse;
import com.example.dacn2_beserver.dto.auth.OtpVerifyRequest;
import com.example.dacn2_beserver.dto.auth.OtpVerifyResponse;
import com.example.dacn2_beserver.exception.OTPExpiredException;
import com.example.dacn2_beserver.exception.OTPInvalidException;
import com.example.dacn2_beserver.exception.OTPTooSoonException;
import com.example.dacn2_beserver.model.auth.OtpRequest;
import com.example.dacn2_beserver.model.auth.UserIdentity;
import com.example.dacn2_beserver.model.enums.*;
import com.example.dacn2_beserver.model.user.User;
import com.example.dacn2_beserver.repository.OtpRequestRepository;
import com.example.dacn2_beserver.repository.UserIdentityRepository;
import com.example.dacn2_beserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OtpAuthService {
    private static final int MAX_ATTEMPTS = 5;
    private static final long OTP_TTL_SECONDS = 5 * 60;
    private static final long MIN_REQUEST_INTERVAL_SECONDS = 30; // chống spam basic

    private final OtpRequestRepository otpRequestRepository;
    private final UserRepository userRepository;
    private final UserIdentityRepository userIdentityRepository;

    private final OtpSender otpSender;
    private final SessionService sessionService;
    private final JwtService jwtService;

    public OtpRequestCreateResponse requestOtp(OtpRequestCreateRequest req) {
        OtpChannel channel = req.getChannel();
        String identifier = req.getIdentifier();

        String normalized = switch (channel) {
            case EMAIL -> IdentifierNormalizer.normalizeEmail(identifier);
            case SMS -> IdentifierNormalizer.normalizePhone(identifier);
        };

        // anti-spam: nếu OTP gần nhất < 30s thì chặn
        otpRequestRepository.findTopByIdentifierNormalizedAndPurposeOrderByCreatedAtDesc(
                normalized, OtpPurpose.LOGIN
        ).ifPresent(last -> {
            if (last.getCreatedAt() != null &&
                    last.getCreatedAt().isAfter(Instant.now().minusSeconds(MIN_REQUEST_INTERVAL_SECONDS))) {
                throw new OTPTooSoonException("OTP requested too soon");
            }
        });

        String code = OtpCrypto.generate6Digits();
        String codeHash = OtpCrypto.sha256(code);

        OtpRequest otp = OtpRequest.builder()
                .channel(channel)
                .purpose(OtpPurpose.LOGIN)
                .identifier(identifier)
                .identifierNormalized(normalized)
                .codeHash(codeHash)
                .status(OtpStatus.PENDING)
                .expiresAt(Instant.now().plusSeconds(OTP_TTL_SECONDS))
                .build();

        otpRequestRepository.save(otp);

        otpSender.send(channel, identifier, code);

        return OtpRequestCreateResponse.builder()
                .expiresInSeconds(OTP_TTL_SECONDS)
                .build();
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest req) {
        String identifier = req.getIdentifier();

        // Vì verify request không gửi channel, mình sẽ tìm OTP theo cả EMAIL & PHONE normalization
        String emailNorm = IdentifierNormalizer.normalizeEmail(identifier);
        String phoneNorm = IdentifierNormalizer.normalizePhone(identifier);

        Instant now = Instant.now();

        List<OtpRequest> candidatesEmail = otpRequestRepository
                .findAllByIdentifierNormalizedAndPurposeAndStatusAndExpiresAtAfter(
                        emailNorm, OtpPurpose.LOGIN, OtpStatus.PENDING, now
                );

        List<OtpRequest> candidatesPhone = otpRequestRepository
                .findAllByIdentifierNormalizedAndPurposeAndStatusAndExpiresAtAfter(
                        phoneNorm, OtpPurpose.LOGIN, OtpStatus.PENDING, now
                );

        OtpRequest otp = pickLatest(candidatesEmail, candidatesPhone);
        if (otp == null) {
            throw new OTPExpiredException("OTP expired");
            // nếu bạn thêm ErrorCode.OTP_EXPIRED thì dùng OTP_EXPIRED
        }

        String codeHash = OtpCrypto.sha256(req.getCode());
        if (!codeHash.equals(otp.getCodeHash())) {
            otp.setAttempts(otp.getAttempts() + 1);
            if (otp.getAttempts() >= MAX_ATTEMPTS) {
                otp.setStatus(OtpStatus.LOCKED);
                otp.setLockedAt(now);
            }
            otpRequestRepository.save(otp);

            throw new OTPInvalidException("OTP code invalid");
            // nếu bạn thêm ErrorCode.OTP_INVALID thì dùng OTP_INVALID
        }

        // verified
        otp.setStatus(OtpStatus.VERIFIED);
        otp.setVerifiedAt(now);
        otpRequestRepository.save(otp);

        IdentityProvider provider = (otp.getChannel() == OtpChannel.EMAIL)
                ? IdentityProvider.EMAIL
                : IdentityProvider.PHONE;

        String normalized = otp.getIdentifierNormalized();

        // 1) tìm user theo identity
        String userId = userIdentityRepository.findByProviderAndNormalized(provider, normalized)
                .map(UserIdentity::getUserId)
                .orElseGet(() -> createUserAndIdentity(provider, otp.getIdentifier(), normalized));

        // 2) tạo session + refresh token (30 ngày)
        var platform = DevicePlatform.WEB; // tạm, sau này map theo header hoặc body
        var issued = sessionService.createSession(userId, req.getDeviceId(), platform);

        // 3) JWT access token
        String accessToken = jwtService.generateAccessToken(userId, issued.session().getId());

        return OtpVerifyResponse.builder()
                .userId(userId)
                .sessionId(issued.session().getId())
                .accessToken(accessToken)
                .refreshToken(issued.refreshTokenPlain())
                .expiresInSeconds(jwtService.getExpirationSeconds())
                .build();
    }

    private OtpRequest pickLatest(List<OtpRequest> a, List<OtpRequest> b) {
        OtpRequest best = null;
        for (OtpRequest x : a) best = newer(best, x);
        for (OtpRequest x : b) best = newer(best, x);
        return best;
    }

    private OtpRequest newer(OtpRequest cur, OtpRequest next) {
        if (cur == null) return next;
        if (next == null) return cur;
        if (cur.getCreatedAt() == null) return next;
        if (next.getCreatedAt() == null) return cur;
        return next.getCreatedAt().isAfter(cur.getCreatedAt()) ? next : cur;
    }

    private String createUserAndIdentity(IdentityProvider provider, String identifierRaw, String normalized) {
        User u = User.builder().build();

        if (provider == IdentityProvider.EMAIL) {
            u.setPrimaryEmail(IdentifierNormalizer.normalizeEmail(identifierRaw));
        }
        // PHONE: primaryEmail để null, vẫn ok vì field sparse unique :contentReference[oaicite:13]{index=13}

        u = userRepository.save(u);

        UserIdentity identity = UserIdentity.builder()
                .userId(u.getId())
                .provider(provider)
                .identifier(identifierRaw)
                .normalized(normalized)
                .verified(true)
                .build();

        userIdentityRepository.save(identity);
        return u.getId();
    }
}