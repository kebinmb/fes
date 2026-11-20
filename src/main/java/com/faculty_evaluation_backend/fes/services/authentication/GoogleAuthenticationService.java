package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.dto.authentication.GoogleLoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Role;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Status;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.services.oauth.GoogleTokenVerifierService;
import com.faculty_evaluation_backend.fes.services.oauth.GoogleTokenVerifierService.GoogleUserInfo;
import com.faculty_evaluation_backend.fes.services.token.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthenticationService {

    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final UserAccountsRepository userAccountsRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public record GoogleAuthResult(
            UserAccounts user,
            String accessToken,
            String refreshToken,
            boolean isAdminRole
    ) {}

    @Transactional(transactionManager = "primaryTransactionManager")
    public GoogleAuthResult authenticate(GoogleLoginRequest request, HttpServletRequest httpRequest) {
        GoogleUserInfo googleUser = googleTokenVerifierService.verifyToken(request.idToken());
        String email = googleUser.email();

        List<Role> allowedRoles = List.of(
                Role.DEAN,
                Role.PROGRAM_CHAIR,
                Role.ADMIN,
                Role.HR
        );

        UserAccounts user = userAccountsRepository.findByEmailAndRoleInAndStatus(email, allowedRoles, Status.ACTIVE)
                .orElseThrow(() -> {
                    log.warn(">>> [GOOGLE AUTH] No active account found for Google email: {}", email);
                    return new UnauthorizedException("No active account associated with Google email: " + email);
                });

        if (!Boolean.TRUE.equals(user.getIsEnabled())) {
            log.warn(">>> [GOOGLE AUTH] Account disabled for user: {}", email);
            throw new UnauthorizedException("Account disabled");
        }

        if (Boolean.TRUE.equals(user.getIsLocked())) {
            log.warn(">>> [GOOGLE AUTH] Account locked for user: {}", email);
            throw new UnauthorizedException("Account locked");
        }

        boolean isAdminRole = (user.getRole() == Role.ADMIN || user.getRole() == Role.HR);
        String accessToken;
        String refreshToken;

        if (isAdminRole) {
            accessToken = jwtService.generateAccessTokenForAdministrator(user.getUserId());
            refreshToken = jwtService.generateRefreshTokenForAdministrator(user.getUserId());
        } else {
            accessToken = jwtService.generateAccessTokenForSupervisor(user.getUserId());
            refreshToken = jwtService.generateRefreshTokenForSupervisor(user.getUserId());
        }

        refreshTokenService.createSession(
                user.getUserId().toString(),
                refreshToken,
                httpRequest
        );

        user.lastLoginNow();
        userAccountsRepository.save(user);

        log.info(">>> [GOOGLE AUTH] Successfully authenticated user: {} with role: {}", email, user.getRole());

        return new GoogleAuthResult(user, accessToken, refreshToken, isAdminRole);
    }
}
