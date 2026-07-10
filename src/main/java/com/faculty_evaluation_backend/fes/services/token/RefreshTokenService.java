package com.faculty_evaluation_backend.fes.services.token;

import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.entities.tokens.RefreshToken;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.tokens.RefreshTokenRepository;
import com.faculty_evaluation_backend.fes.utilities.token.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Duration TOUCH_THROTTLE = Duration.ofSeconds(30);

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Transactional(transactionManager = "primaryTransactionManager")
    public void createSession(
            String userId,
            String rawRefreshToken,
            HttpServletRequest request
    ) {
        Instant now = Instant.now();

        refreshTokenRepository.revokeAllByUserId(userId);
        refreshTokenRepository.save(
                buildRefreshToken(
                        userId,
                        rawRefreshToken,
                        deviceFingerprint(request),
                        now
                )
        );
    }

    public RefreshToken buildRefreshToken(
            String userId,
            String rawRefreshToken,
            String deviceFingerprint,
            Instant now
    ) {
        return RefreshToken.builder()
                .userId(userId)
                .tokenHash(TokenHashUtil.sha256(rawRefreshToken))
                .expiryDate(now.plusMillis(jwtConfig.getRefreshExpiration()))
                .lastActivityAt(now)
                .revoked(false)
                .deviceInfo(deviceFingerprint)
                .build();
    }

    @Transactional(transactionManager = "primaryTransactionManager")
    public boolean validateAndTouchActiveSession(
            String rawRefreshToken,
            boolean hasUserActivity
    ) {
        Instant now = Instant.now();
        RefreshToken storedToken = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(
                        TokenHashUtil.sha256(rawRefreshToken)
                )
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid refresh token")
                );

        validateSession(storedToken, now, hasUserActivity);

        Instant lastActivityAt = storedToken.getLastActivityAt();
        if (hasUserActivity
                && (lastActivityAt == null
                || lastActivityAt.plus(TOUCH_THROTTLE).isBefore(now))) {

            storedToken.setLastActivityAt(now);
            refreshTokenRepository.save(storedToken);
            return true;
        }

        return false;
    }

    public void validateSession(
            RefreshToken storedToken,
            Instant now
    ) {
        validateSession(storedToken, now, false);
    }

    public void validateSession(
            RefreshToken storedToken,
            Instant now,
            boolean hasUserActivity
    ) {
        if (storedToken.getExpiryDate()
                .minusSeconds(5)
                .isBefore(now)) {

            revoke(storedToken);
            throw new UnauthorizedException("Refresh token expired.");
        }

        Instant lastActivityAt = storedToken.getLastActivityAt();

        if (!hasUserActivity
                && (lastActivityAt == null
                || lastActivityAt
                .plusMillis(jwtConfig.getIdleTimeout())
                .isBefore(now))) {

            revoke(storedToken);
            throw new UnauthorizedException("Session idle timeout expired.");
        }
    }

    public String deviceFingerprint(HttpServletRequest request) {
        String rawDevice = request.getHeader("User-Agent");

        return TokenHashUtil.sha256(
                rawDevice != null
                        ? rawDevice
                        : "unknown"
        ).substring(0, 16);
    }

    private void revoke(RefreshToken storedToken) {
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
    }
}
