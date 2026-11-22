package com.faculty_evaluation_backend.fes.services.oauth;

import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierServiceTest {

    @Mock
    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    private static final String CONFIGURED_CLIENT_ID = "30269263780-test.apps.googleusercontent.com";
    private GoogleTokenVerifierService verifierService;

    @BeforeEach
    void setUp() {
        verifierService = new GoogleTokenVerifierService(CONFIGURED_CLIENT_ID, jwtProcessor);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when googleClientId is blank or null")
    void verifyToken_WhenGoogleClientIdIsBlank_ThrowsIllegalStateException() {
        GoogleTokenVerifierService unconfiguredService = new GoogleTokenVerifierService("", jwtProcessor);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> unconfiguredService.verifyToken("any-token")
        );

        assertThat(ex.getMessage()).contains("Google OAuth client ID is not configured");
    }

    @Test
    @DisplayName("Should successfully verify valid Google token and return GoogleUserInfo")
    void verifyToken_WhenValidToken_ReturnsGoogleUserInfo() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .audience(CONFIGURED_CLIENT_ID)
                .expirationTime(new Date(System.currentTimeMillis() + 300_000))
                .subject("google-sub-12345")
                .claim("email", "faculty@chmsu.edu.ph")
                .claim("email_verified", true)
                .claim("name", "Faculty Member")
                .build();

        when(jwtProcessor.process("valid-google-token", null)).thenReturn(claims);

        var userInfo = verifierService.verifyToken("valid-google-token");

        assertThat(userInfo).isNotNull();
        assertThat(userInfo.email()).isEqualTo("faculty@chmsu.edu.ph");
        assertThat(userInfo.name()).isEqualTo("Faculty Member");
        assertThat(userInfo.sub()).isEqualTo("google-sub-12345");
        assertThat(userInfo.emailVerified()).isTrue();
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when issuer is not Google")
    void verifyToken_WhenIssuerInvalid_ThrowsIllegalArgumentException() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://rogue-auth.com")
                .audience(CONFIGURED_CLIENT_ID)
                .expirationTime(new Date(System.currentTimeMillis() + 300_000))
                .claim("email", "faculty@chmsu.edu.ph")
                .claim("email_verified", true)
                .build();

        when(jwtProcessor.process("untrusted-issuer-token", null)).thenReturn(claims);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> verifierService.verifyToken("untrusted-issuer-token")
        );

        assertThat(ex.getMessage()).contains("Invalid Google token issuer");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when audience does not match configured client ID")
    void verifyToken_WhenAudienceMismatch_ThrowsIllegalArgumentException() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .audience("another-app-client-id.apps.googleusercontent.com")
                .expirationTime(new Date(System.currentTimeMillis() + 300_000))
                .claim("email", "faculty@chmsu.edu.ph")
                .claim("email_verified", true)
                .build();

        when(jwtProcessor.process("mismatched-aud-token", null)).thenReturn(claims);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> verifierService.verifyToken("mismatched-aud-token")
        );

        assertThat(ex.getMessage()).contains("Google token audience mismatch");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when audience is null")
    void verifyToken_WhenAudienceNull_ThrowsIllegalArgumentException() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .expirationTime(new Date(System.currentTimeMillis() + 300_000))
                .claim("email", "faculty@chmsu.edu.ph")
                .claim("email_verified", true)
                .build();

        when(jwtProcessor.process("null-aud-token", null)).thenReturn(claims);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> verifierService.verifyToken("null-aud-token")
        );

        assertThat(ex.getMessage()).contains("Google token audience mismatch");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when token has expired")
    void verifyToken_WhenTokenExpired_ThrowsIllegalArgumentException() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .audience(CONFIGURED_CLIENT_ID)
                .expirationTime(new Date(System.currentTimeMillis() - 10_000))
                .claim("email", "faculty@chmsu.edu.ph")
                .claim("email_verified", true)
                .build();

        when(jwtProcessor.process("expired-token", null)).thenReturn(claims);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> verifierService.verifyToken("expired-token")
        );

        assertThat(ex.getMessage()).contains("Google token has expired");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when email is unverified")
    void verifyToken_WhenEmailUnverified_ThrowsIllegalArgumentException() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer("https://accounts.google.com")
                .audience(CONFIGURED_CLIENT_ID)
                .expirationTime(new Date(System.currentTimeMillis() + 300_000))
                .claim("email", "faculty@chmsu.edu.ph")
                .claim("email_verified", false)
                .build();

        when(jwtProcessor.process("unverified-email-token", null)).thenReturn(claims);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> verifierService.verifyToken("unverified-email-token")
        );

        assertThat(ex.getMessage()).contains("Google account email is missing or not verified");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when processing / cryptographic check fails")
    void verifyToken_WhenJwtProcessorThrows_WrapsInIllegalArgumentException() throws Exception {
        when(jwtProcessor.process("corrupted-token", null))
                .thenThrow(new RuntimeException("Signature verification failed"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> verifierService.verifyToken("corrupted-token")
        );

        assertThat(ex.getMessage()).contains("Failed to verify Google token: Signature verification failed");
    }
}
