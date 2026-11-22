package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.dto.authentication.GoogleLoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Role;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.RateLimitExceededException;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.services.oauth.GoogleTokenVerifierService;
import com.faculty_evaluation_backend.fes.services.oauth.GoogleTokenVerifierService.GoogleUserInfo;
import com.faculty_evaluation_backend.fes.services.rateLimiting.RateLimitingService;
import com.faculty_evaluation_backend.fes.services.token.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleAuthenticationServiceTest {

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

    @Mock
    private UserAccountsRepository userAccountsRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private RateLimitingService rateLimitingService;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private GoogleAuthenticationService googleAuthenticationService;

    private static final String VALID_TOKEN = "dummy-google-id-token";

    @Test
    @DisplayName("Should successfully authenticate admin user and return admin JWT tokens")
    void authenticate_WhenValidAdmin_ShouldSucceedAndReturnAdminTokens() {
        GoogleLoginRequest request = new GoogleLoginRequest(VALID_TOKEN);
        GoogleUserInfo googleUser = new GoogleUserInfo("admin@chmsu.edu.ph", "Admin User", "sub-123", true);

        UserAccounts adminUser = UserAccounts.builder()
                .userId(1L)
                .username("admin123")
                .email("admin@chmsu.edu.ph")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .isEnabled(true)
                .isLocked(false)
                .build();

        when(googleTokenVerifierService.verifyToken(VALID_TOKEN)).thenReturn(googleUser);
        when(userAccountsRepository.findByEmailAndRoleInAndStatus(eq("admin@chmsu.edu.ph"), anyList(), eq(Status.ACTIVE)))
                .thenReturn(Optional.of(adminUser));
        when(jwtService.generateAccessTokenForAdministrator(1L)).thenReturn("admin-access-token");
        when(jwtService.generateRefreshTokenForAdministrator(1L)).thenReturn("admin-refresh-token");

        var result = googleAuthenticationService.authenticate(request, httpRequest);

        assertThat(result).isNotNull();
        assertThat(result.isAdminRole()).isTrue();
        assertThat(result.accessToken()).isEqualTo("admin-access-token");
        assertThat(result.refreshToken()).isEqualTo("admin-refresh-token");
        assertThat(result.user().getUserId()).isEqualTo(1L);

        verify(rateLimitingService).consumeFacultyRequest(null, "AUTHENTICATE_GOOGLE");
        verify(refreshTokenService).createSession("1", "admin-refresh-token", httpRequest);
        verify(userAccountsRepository).save(adminUser);
    }

    @Test
    @DisplayName("Should successfully authenticate supervisor (DEAN) and return supervisor JWT tokens")
    void authenticate_WhenValidSupervisor_ShouldSucceedAndReturnSupervisorTokens() {
        GoogleLoginRequest request = new GoogleLoginRequest(VALID_TOKEN);
        GoogleUserInfo googleUser = new GoogleUserInfo("dean@chmsu.edu.ph", "Dean User", "sub-456", true);

        UserAccounts supervisorUser = UserAccounts.builder()
                .userId(2L)
                .username("dean123")
                .email("dean@chmsu.edu.ph")
                .role(Role.DEAN)
                .status(Status.ACTIVE)
                .isEnabled(true)
                .isLocked(false)
                .build();

        when(googleTokenVerifierService.verifyToken(VALID_TOKEN)).thenReturn(googleUser);
        when(userAccountsRepository.findByEmailAndRoleInAndStatus(eq("dean@chmsu.edu.ph"), anyList(), eq(Status.ACTIVE)))
                .thenReturn(Optional.of(supervisorUser));
        when(jwtService.generateAccessTokenForSupervisor(2L)).thenReturn("supervisor-access-token");
        when(jwtService.generateRefreshTokenForSupervisor(2L)).thenReturn("supervisor-refresh-token");

        var result = googleAuthenticationService.authenticate(request, httpRequest);

        assertThat(result).isNotNull();
        assertThat(result.isAdminRole()).isFalse();
        assertThat(result.accessToken()).isEqualTo("supervisor-access-token");
        assertThat(result.refreshToken()).isEqualTo("supervisor-refresh-token");
        assertThat(result.user().getUserId()).isEqualTo(2L);

        verify(rateLimitingService).consumeFacultyRequest(null, "AUTHENTICATE_GOOGLE");
        verify(refreshTokenService).createSession("2", "supervisor-refresh-token", httpRequest);
        verify(userAccountsRepository).save(supervisorUser);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when no active account matches the Google email")
    void authenticate_WhenUserNotFound_ShouldThrowUnauthorizedException() {
        GoogleLoginRequest request = new GoogleLoginRequest(VALID_TOKEN);
        GoogleUserInfo googleUser = new GoogleUserInfo("stranger@gmail.com", "Stranger", "sub-789", true);

        when(googleTokenVerifierService.verifyToken(VALID_TOKEN)).thenReturn(googleUser);
        when(userAccountsRepository.findByEmailAndRoleInAndStatus(eq("stranger@gmail.com"), anyList(), eq(Status.ACTIVE)))
                .thenReturn(Optional.empty());

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> googleAuthenticationService.authenticate(request, httpRequest)
        );

        assertThat(exception.getMessage()).contains("No active account associated with Google email");
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when account is disabled")
    void authenticate_WhenAccountDisabled_ShouldThrowUnauthorizedException() {
        GoogleLoginRequest request = new GoogleLoginRequest(VALID_TOKEN);
        GoogleUserInfo googleUser = new GoogleUserInfo("disabled@chmsu.edu.ph", "Disabled User", "sub-disabled", true);

        UserAccounts disabledUser = UserAccounts.builder()
                .userId(3L)
                .username("disabled123")
                .email("disabled@chmsu.edu.ph")
                .role(Role.ADMIN)
                .status(Status.ACTIVE)
                .isEnabled(false)
                .isLocked(false)
                .build();

        when(googleTokenVerifierService.verifyToken(VALID_TOKEN)).thenReturn(googleUser);
        when(userAccountsRepository.findByEmailAndRoleInAndStatus(eq("disabled@chmsu.edu.ph"), anyList(), eq(Status.ACTIVE)))
                .thenReturn(Optional.of(disabledUser));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> googleAuthenticationService.authenticate(request, httpRequest)
        );

        assertThat(exception.getMessage()).isEqualTo("Account disabled");
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when account is locked")
    void authenticate_WhenAccountLocked_ShouldThrowUnauthorizedException() {
        GoogleLoginRequest request = new GoogleLoginRequest(VALID_TOKEN);
        GoogleUserInfo googleUser = new GoogleUserInfo("locked@chmsu.edu.ph", "Locked User", "sub-locked", true);

        UserAccounts lockedUser = UserAccounts.builder()
                .userId(4L)
                .username("locked123")
                .email("locked@chmsu.edu.ph")
                .role(Role.PROGRAM_CHAIR)
                .status(Status.ACTIVE)
                .isEnabled(true)
                .isLocked(true)
                .build();

        when(googleTokenVerifierService.verifyToken(VALID_TOKEN)).thenReturn(googleUser);
        when(userAccountsRepository.findByEmailAndRoleInAndStatus(eq("locked@chmsu.edu.ph"), anyList(), eq(Status.ACTIVE)))
                .thenReturn(Optional.of(lockedUser));

        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> googleAuthenticationService.authenticate(request, httpRequest)
        );

        assertThat(exception.getMessage()).isEqualTo("Account locked");
        verifyNoInteractions(jwtService, refreshTokenService);
    }

    @Test
    @DisplayName("Should block request and throw RateLimitExceededException before verifying token if rate limited")
    void authenticate_WhenRateLimitExceeded_ShouldBlockEarly() {
        GoogleLoginRequest request = new GoogleLoginRequest(VALID_TOKEN);
        doThrow(new RateLimitExceededException("Too many requests. Try again later."))
                .when(rateLimitingService).consumeFacultyRequest(null, "AUTHENTICATE_GOOGLE");

        assertThrows(
                RateLimitExceededException.class,
                () -> googleAuthenticationService.authenticate(request, httpRequest)
        );

        verifyNoInteractions(googleTokenVerifierService, userAccountsRepository, jwtService, refreshTokenService);
    }
}
