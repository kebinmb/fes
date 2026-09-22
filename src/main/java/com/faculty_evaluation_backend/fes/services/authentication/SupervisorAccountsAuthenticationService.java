package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.dto.authentication.ChangePasswordRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.CurrentUserResponse;
import com.faculty_evaluation_backend.fes.dto.authentication.UserAuthenticationResponse;
import com.faculty_evaluation_backend.fes.dto.authentication.LoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.services.rateLimiting.RateLimitingService;
import com.faculty_evaluation_backend.fes.services.token.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupervisorAccountsAuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserAccountsRepository userAccountsRepository;
    private final RateLimitingService rateLimitingService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final RefreshTokenService refreshTokenService;

    @Transactional(transactionManager = "primaryTransactionManager")
    public UserAuthenticationResponse login(
            LoginRequest loginRequest,
            HttpServletRequest request
    ) {

        String identifier = loginRequest.getUsernameOrEmail();

        rateLimitingService.consumeFacultyRequest(identifier, "AUTHENTICATE_SUPERVISOR");

        log.info("Login attempt for identifier: {}", identifier);

        try {
            Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(identifier, loginRequest.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUserId();

            String accessToken = jwtService.generateAccessTokenForSupervisor(userId);
            String refreshToken = jwtService.generateRefreshTokenForSupervisor(userId);
            refreshTokenService.createSession(userId.toString(), refreshToken, request);

            UserAccounts userAccount = userDetails.getUser();
            userAccount.lastLoginNow();
            userAccountsRepository.save(userAccount);

            log.info("Login successful for userId = {}", userAccount.getUserId());

            return UserAuthenticationResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType("Bearer").expiresIn(jwtConfig.getExpiration()).evaluatorId(userId.toString()).college(userAccount.getCollege()).programs(userAccount.getPrograms()).requiresPasswordChange(userAccount.getPasswordChangedAt() == null).build();

        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for identifier: {}", identifier);
            throw new BadCredentialsException("Invalid username or password.");

        } catch (DisabledException ex) {
            log.warn("Login attempt for disabled account: {}", identifier);
            throw new DisabledException("Account is disabled");

        } catch (LockedException ex) {
            log.warn("Login attempt for locked account: {}", identifier);
            throw new LockedException("Account is locked");
        }
    }

    public CurrentUserResponse getSupervisorProfile(Long userId, String role) {
        UserAccounts user = findByUserId(userId);
        return CurrentUserResponse.builder()
                .authenticated(true)
                .userId(String.valueOf(user.getUserId()))
                .evaluatorId(String.valueOf(user.getUserId()))
                .role(role)
                .college(user.getCollege() != null ? user.getCollege().name() : null)
                .program(user.getPrograms() != null ? user.getPrograms().name() : null)
                .requiresPasswordChange(user.getPasswordChangedAt() == null)
                .build();
    }

    public UserAccounts findByUserId(Long userId) {

        return userAccountsRepository
                .findById(userId)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        ));
    }

    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        UserAccounts user = userAccountsRepository
                .findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found."));

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    "Current password is incorrect.");
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmNewPassword())) {

            throw new BadRequestException(
                    "New password and confirmation password do not match");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            throw new BadRequestException(
                    "New password must be different from current password");
        }

        if (request.getNewPassword().length() < 8) {

            throw new BadRequestException(
                    "Password must be at least 8 characters long");
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        user.setPasswordChangedAt(
                Instant.now()
        );

        userAccountsRepository.save(user);
    }
}
