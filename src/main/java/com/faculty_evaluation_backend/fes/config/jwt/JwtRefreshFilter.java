package com.faculty_evaluation_backend.fes.config.jwt;

import com.faculty_evaluation_backend.fes.entities.tokens.RefreshToken;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.tokens.RefreshTokenRepository;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.utilities.token.TokenHashUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRefreshFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {

            String accessToken = null;
            String refreshToken = null;

            String role = null;

            String accessCookieName = null;
            String refreshCookieName = null;

            // =========================================
            // ADMINISTRATOR
            // =========================================
            refreshToken = extractCookie(request, "administrator_refresh");

            if (refreshToken != null) {

                accessToken = extractCookie(request, "administrator_access");

                role = "ROLE_ADMIN";

                accessCookieName = "administrator_access";
                refreshCookieName = "administrator_refresh";
            }

            // =========================================
            // SUPERVISOR
            // =========================================
            if (refreshToken == null) {

                refreshToken = extractCookie(request, "supervisor_refresh");

                if (refreshToken != null) {

                    accessToken = extractCookie(request, "supervisor_access");

                    role = "ROLE_SUPERVISOR";

                    accessCookieName = "supervisor_access";
                    refreshCookieName = "supervisor_refresh";
                }
            }

            // =========================================
            // STUDENT
            // =========================================
            if (refreshToken == null) {

                refreshToken = extractCookie(request, "student_refresh");

                if (refreshToken != null) {

                    accessToken = extractCookie(request, "student_access");

                    role = "ROLE_STUDENT";

                    accessCookieName = "student_access";
                    refreshCookieName = "student_refresh";
                }
            }

            boolean shouldRefresh =
                    accessToken == null
                            || !jwtService.isAccessTokenValid(accessToken)
                            || jwtService.isTokenExpired(accessToken);

            if (shouldRefresh && refreshToken != null) {

                String tokenHash =
                        TokenHashUtil.sha256(refreshToken);

                RefreshToken storedToken =
                        refreshTokenRepository
                                .findValidTokenForUpdate(tokenHash)
                                .orElseThrow(() ->
                                        new UnauthorizedException(
                                                "Invalid refresh token"
                                        )
                                );

                if (storedToken.getExpiryDate()
                        .minusSeconds(5)
                        .isBefore(Instant.now())) {

                    throw new UnauthorizedException(
                            "Refresh token expired."
                    );
                }

                if (!jwtService.isRefreshTokenValid(refreshToken)) {

                    throw new UnauthorizedException(
                            "Invalid refresh token"
                    );
                }

                Claims claims =
                        jwtService.extractAllClaims(refreshToken);

                String userId = claims.getSubject();

                String type =
                        claims.get("type", String.class);

                // =========================================
                // TOKEN TYPE VALIDATION
                // =========================================
                if (role.equals("ROLE_STUDENT")
                        && !"student_refresh".equals(type)) {

                    throw new UnauthorizedException(
                            "Invalid token type"
                    );
                }

                if (role.equals("ROLE_SUPERVISOR")
                        && !"faculty_refresh".equals(type)) {

                    throw new UnauthorizedException(
                            "Invalid token type"
                    );
                }

                if (role.equals("ROLE_ADMIN")
                        && !"administrator_refresh".equals(type)) {

                    throw new UnauthorizedException(
                            "Invalid token type"
                    );
                }

                // =========================================
                // DEVICE FINGERPRINT
                // =========================================
                String requestDevice =
                        request.getHeader("User-Agent");

                String deviceFingerprint =
                        TokenHashUtil.sha256(
                                requestDevice != null
                                        ? requestDevice
                                        : "unknown"
                        ).substring(0, 16);

                String storedFingerprint =
                        storedToken.getDeviceInfo();

                if (storedFingerprint != null
                        && !storedFingerprint.equals(deviceFingerprint)) {

                    throw new UnauthorizedException(
                            "Device mismatch"
                    );
                }

                // =========================================
                // REVOKE OLD TOKEN
                // =========================================
                storedToken.setRevoked(true);

                refreshTokenRepository.save(storedToken);

                // =========================================
                // GENERATE NEW TOKENS
                // =========================================
                String newAccessToken;
                String newRefreshToken;

                if (role.equals("ROLE_STUDENT")) {

                    newAccessToken =
                            jwtService.generateAccessTokenForStudent(
                                    userId
                            );

                    newRefreshToken =
                            jwtService.generateRefreshTokenForStudent(
                                    userId
                            );

                } else if (role.equals("ROLE_SUPERVISOR")) {

                    newAccessToken =
                            jwtService.generateAccessTokenForSupervisor(
                                    Long.valueOf(userId)
                            );

                    newRefreshToken =
                            jwtService.generateRefreshTokenForSupervisor(
                                    Long.valueOf(userId)
                            );

                } else {

                    newAccessToken =
                            jwtService.generateAccessTokenForAdministrator(
                                    Long.valueOf(userId)
                            );

                    newRefreshToken =
                            jwtService.generateRefreshTokenForAdministrator(
                                    Long.valueOf(userId)
                            );
                }

                // =========================================
                // SAVE NEW REFRESH TOKEN
                // =========================================
                refreshTokenRepository.save(
                        RefreshToken.builder()
                                .userId(userId)
                                .tokenHash(
                                        TokenHashUtil.sha256(
                                                newRefreshToken
                                        )
                                )
                                .expiryDate(
                                        Instant.now()
                                                .plusMillis(
                                                        jwtConfig
                                                                .getRefreshExpiration()
                                                )
                                )
                                .revoked(false)
                                .deviceInfo(deviceFingerprint)
                                .build()
                );

                // =========================================
                // SET NEW COOKIES
                // =========================================
                response.addHeader(
                        "Set-Cookie",
                        buildAccessTokenCookie(
                                newAccessToken,
                                accessCookieName
                        ).toString()
                );

                response.addHeader(
                        "Set-Cookie",
                        buildRefreshTokenCookie(
                                newRefreshToken,
                                refreshCookieName
                        ).toString()
                );

                log.info("Token rotated for {}", userId);
            }

        } catch (Exception e) {

            log.warn("Refresh failed: {}", e.getMessage());

            response.addHeader(
                    "Set-Cookie",
                    deleteCookie("student_access").toString()
            );

            response.addHeader(
                    "Set-Cookie",
                    deleteCookie("student_refresh").toString()
            );

            response.addHeader(
                    "Set-Cookie",
                    deleteCookie("supervisor_access").toString()
            );

            response.addHeader(
                    "Set-Cookie",
                    deleteCookie("supervisor_refresh").toString()
            );

            response.addHeader(
                    "Set-Cookie",
                    deleteCookie("administrator_access").toString()
            );

            response.addHeader(
                    "Set-Cookie",
                    deleteCookie("administrator_refresh").toString()
            );
        }

        filterChain.doFilter(request, response);
    }

    private String extractCookie(
            HttpServletRequest request,
            String cookieName
    ) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }

    private ResponseCookie buildAccessTokenCookie(
            String token,
            String name
    ) {

        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(
                        Math.max(
                                1,
                                jwtConfig.getExpiration() / 1000
                        )
                )
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(
            String token,
            String name
    ) {

        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(
                        jwtConfig.getRefreshExpiration() / 1000
                )
                .build();
    }

    private ResponseCookie deleteCookie(String name) {

        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {
        return false;
    }
}