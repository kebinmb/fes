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
        try{
            String accessToken = extractCookie(request, "student_access");
            String refreshToken = extractCookie(request, "student_refresh");
            boolean shouldRefresh = accessToken == null
                    || !jwtService.isAccessTokenValid(accessToken)
                    || jwtService.isTokenExpired(accessToken);
            if (shouldRefresh && refreshToken != null) {
                String tokenHash = TokenHashUtil.sha256(refreshToken);
                RefreshToken storedToken = refreshTokenRepository
                        .findValidTokenForUpdate(tokenHash)
                        .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
                if (storedToken.getExpiryDate().minusSeconds(5).isBefore(Instant.now())) {
                    throw new UnauthorizedException("Refresh token expired.");
                }
                if(!jwtService.isRefreshTokenValid(refreshToken)){
                    throw new UnauthorizedException("Invalid refresh token");
                }
                Claims claims = jwtService.extractAllClaims(refreshToken);
                String studentId = claims.getSubject();
                String type = claims.get("type",String.class);
                if(!"student_refresh".equals(type)){
                    throw new UnauthorizedException("Invalid token type.");
                }
                String requestDevice = request.getHeader("User-Agent");
                String deviceFingerprint = TokenHashUtil.sha256(
                        requestDevice != null ? requestDevice : "unknown"
                ).substring(0, 16);
                String storedFingerprint = storedToken.getDeviceInfo();

                if (storedFingerprint != null && !storedFingerprint.equals(deviceFingerprint)) {
                    throw new UnauthorizedException("Device mismatch");
                }
                storedToken.setRevoked(true);
                refreshTokenRepository.save(storedToken);

                String newRefreshToken = jwtService.generateRefreshTokenForStudent(studentId);
                String newAccessToken = jwtService.generateAccessTokenForStudent(studentId);
                refreshTokenRepository.save(
                        RefreshToken.builder()
                                .userId(studentId)
                                .tokenHash(TokenHashUtil.sha256(newRefreshToken))
                                .expiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpiration()))
                                .revoked(false)
                                .deviceInfo(deviceFingerprint)
                                .build()
                );
                response.addHeader("Set-Cookie", buildAccessTokenCookie(newAccessToken).toString());
                response.addHeader("Set-Cookie", buildRefreshTokenCookie(newRefreshToken).toString());
                log.info("Token rotated for student {}", studentId);
            }
        }catch (Exception e){
            log.warn("Refresh failed: {}", e.getMessage());

            response.addHeader("Set-Cookie", ResponseCookie.from("student_access", "")
                    .maxAge(0).path("/").build().toString());

            response.addHeader("Set-Cookie", ResponseCookie.from("student_refresh", "")
                    .maxAge(0).path("/").build().toString());
        }
        filterChain.doFilter(request,response);
    }

    private String extractCookie(HttpServletRequest request, String cookieName){
        if(request.getCookies() == null) return null;
        for(Cookie cookie:request.getCookies()){
            if(cookieName.equals(cookie.getName())){
                return cookie.getValue();
            }
        }
        return null;
    }

    private ResponseCookie buildAccessTokenCookie(String token){
        return ResponseCookie.from("student_access", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Math.max(1, jwtConfig.getExpiration() / 1000))
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String token){
        return ResponseCookie.from("student_refresh", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(jwtConfig.getRefreshExpiration() / 1000)
                .build();
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !path.equals("/auth/refresh");
    }
}
