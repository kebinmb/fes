package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.dto.authentication.UserAuthenticationResponse;
import com.faculty_evaluation_backend.fes.dto.authentication.LoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.entities.primary.enums.Role;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.services.rateLimiting.RateLimitingService;
import com.faculty_evaluation_backend.fes.services.token.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdministratorAccountsAuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;
    private final UserAccountsRepository userAccountsRepository;
    private final RefreshTokenService refreshTokenService;
    private final RateLimitingService rateLimitingService;

    @Transactional(transactionManager = "primaryTransactionManager")
    public UserAuthenticationResponse login(
            LoginRequest loginRequest,
            HttpServletRequest request
    ){
        rateLimitingService.consumeFacultyRequest(
                loginRequest.getUsernameOrEmail(),
                "AUTHENTICATE_ADMINISTRATOR"
        );
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        UserAccounts user = customUserDetails.getUser();
        if(user.getRole() != Role.ADMIN){
            throw new UnauthorizedException("Access denied");
        }
        String accessToken = jwtService.generateAccessTokenForAdministrator(user.getUserId());
        String refreshToken = jwtService.generateRefreshTokenForAdministrator(user.getUserId());
        refreshTokenService.createSession(user.getUserId().toString(), refreshToken, request);
        user.lastLoginNow();
        userAccountsRepository.save(user);

        return UserAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration())
                .evaluatorId(user.getUserId().toString())
                .build();
    }
}
