package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.dto.authentication.FacultyAuthenticationResponse;
import com.faculty_evaluation_backend.fes.dto.authentication.LoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.services.rateLimiting.RateLimitingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SupervisorAccountsAuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final UserAccountsRepository userAccountsRepository;
    private final RateLimitingService rateLimitingService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    @AuditableAction(action = "AUTHENTICATE_SUPERVISOR", entity = "SUPERVISOR_AUTHENTICATION")
    @Transactional(transactionManager = "primaryTransactionManager")
    public FacultyAuthenticationResponse login(LoginRequest loginRequest){

        String identifier = loginRequest.getUsernameOrEmail();

        rateLimitingService.consumeFacultyRequest(identifier, "AUTHENTICATE_SUPERVISOR");

        log.info("Login attempt for identifier: {}", identifier);

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            identifier,
                            loginRequest.getPassword()
                    )
            );

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Long userId = userDetails.getUserId();

            String accessToken = jwtService.generateAccessTokenForSupervisor(userId);
            String refreshToken = jwtService.generateRefreshTokenForSupervisor(userId);

            UserAccounts userAccount = userDetails.getUser();
            userAccount.lastLoginNow();
            userAccountsRepository.save(userAccount);

            log.info("Login successful for userId = {}", userAccount.getUserId());

            return FacultyAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtConfig.getExpiration())
                    .evaluatorId(userId.toString())
                    .build();

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
}
