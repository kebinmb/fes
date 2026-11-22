package com.faculty_evaluation_backend.fes.services.oauth;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class GoogleTokenVerifierService {

    @Value("${GOOGLE_CLIENT_ID:${spring.security.oauth2.client.registration.google.client-id:}}")
    private String googleClientId;

    private static final String GOOGLE_CERTS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final List<String> GOOGLE_ISSUERS = List.of(
            "https://accounts.google.com",
            "accounts.google.com"
    );

    private final JWKSource<SecurityContext> keySource;
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public GoogleTokenVerifierService() {
        try {
            this.keySource = JWKSourceBuilder.<SecurityContext>create(URI.create(GOOGLE_CERTS_URL).toURL())
                    .cache(true)
                    .rateLimited(true)
                    .build();
            this.jwtProcessor = new DefaultJWTProcessor<>();
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource);
            this.jwtProcessor.setJWSKeySelector(keySelector);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Google JWK source", e);
        }
    }

    GoogleTokenVerifierService(String googleClientId, ConfigurableJWTProcessor<SecurityContext> jwtProcessor) {
        this.googleClientId = googleClientId;
        this.keySource = null;
        this.jwtProcessor = jwtProcessor;
    }

    public record GoogleUserInfo(String email, String name, String sub, boolean emailVerified) {}

    public GoogleUserInfo verifyToken(String idTokenString) {
        // 0. Verify server configuration
        if (googleClientId == null || googleClientId.isBlank()) {
            log.error("Google OAuth Client ID is not configured on the backend! Please set GOOGLE_CLIENT_ID in your .env or application.yaml");
            throw new IllegalStateException("Google OAuth client ID is not configured on the server (check GOOGLE_CLIENT_ID in .env)");
        }

        try {
            JWTClaimsSet claims = jwtProcessor.process(idTokenString, null);

            // 1. Verify Issuer
            if (!GOOGLE_ISSUERS.contains(claims.getIssuer())) {
                log.error("Invalid issuer: {}", claims.getIssuer());
                throw new IllegalArgumentException("Invalid Google token issuer: " + claims.getIssuer());
            }

            // 2. Verify Audience (strictly mandatory, null-safe)
            if (claims.getAudience() == null || !claims.getAudience().contains(googleClientId)) {
                log.error("Audience mismatch! Token audience: {}, Configured client ID: {}",
                        claims.getAudience(), googleClientId);
                throw new IllegalArgumentException("Google token audience mismatch. Expected: " + googleClientId + ", but token was issued for: " + claims.getAudience());
            }

            // 3. Verify Expiration
            if (claims.getExpirationTime() != null && claims.getExpirationTime().before(new Date())) {
                log.error("Token has expired at {}", claims.getExpirationTime());
                throw new IllegalArgumentException("Google token has expired");
            }

            String email = claims.getStringClaim("email");
            Boolean emailVerified = claims.getBooleanClaim("email_verified");
            String name = claims.getStringClaim("name");
            String sub = claims.getSubject();

            if (email == null || !Boolean.TRUE.equals(emailVerified)) {
                log.error("Email missing or unverified. Email: {}, Verified: {}", email, emailVerified);
                throw new IllegalArgumentException("Google account email is missing or not verified");
            }

            return new GoogleUserInfo(email, name, sub, emailVerified);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }

    public String getGoogleClientId() {
        return googleClientId;
    }
}
