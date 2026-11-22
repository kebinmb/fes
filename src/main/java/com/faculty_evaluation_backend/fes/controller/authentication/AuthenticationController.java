package com.faculty_evaluation_backend.fes.controller.authentication;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.dto.authentication.ChangePasswordRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.GoogleLoginRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.LoginRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.StudentAccessCodeRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.StudentLoginRequest;
import com.faculty_evaluation_backend.fes.dto.authentication.CurrentUserResponse;
import com.faculty_evaluation_backend.fes.dto.authentication.StudentAccessCodeResponse;
import com.faculty_evaluation_backend.fes.entities.authentication.CustomUserDetails;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.repositories.tokens.RefreshTokenRepository;
import com.faculty_evaluation_backend.fes.services.authentication.AdministratorAccountsAuthenticationService;
import com.faculty_evaluation_backend.fes.services.authentication.GoogleAuthenticationService;
import com.faculty_evaluation_backend.fes.services.authentication.StudentAuthenticationService;
import com.faculty_evaluation_backend.fes.services.authentication.SupervisorAccountsAuthenticationService;
import com.faculty_evaluation_backend.fes.services.oauth.GoogleTokenVerifierService;
import com.faculty_evaluation_backend.fes.utilities.token.TokenHashUtil;
import jakarta.validation.Valid;
import org.springframework.cache.Cache;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {

    private final StudentAuthenticationService studentAuthenticationService;
    private final SupervisorAccountsAuthenticationService supervisorAccountsAuthenticationService;
    private final AdministratorAccountsAuthenticationService administratorAccountsAuthenticationService;
    private final GoogleAuthenticationService googleAuthenticationService;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CacheManager cacheManager;

    private ResponseCookie buildStudentAccessTokenCookie(String token) {
        return buildCookie("student_access", token, Math.max(1, jwtConfig.getExpiration() / 1000));
    }

    private ResponseCookie buildStudentRefreshTokenCookie(String token) {
        return buildCookie("student_refresh", token, jwtConfig.getRefreshExpiration() / 1000);
    }

    private ResponseCookie buildSupervisorAccessTokenCookie(String token) {
        return buildCookie("supervisor_access", token, Math.max(1, jwtConfig.getExpiration() / 1000));
    }

    private ResponseCookie buildSupervisorRefreshTokenCookie(String token) {
        return buildCookie("supervisor_refresh", token, Math.max(1, jwtConfig.getRefreshExpiration() / 1000));
    }

    private ResponseCookie buildAdministratorAccessTokenCookie(String token) {
        return buildCookie("administrator_access", token, Math.max(1, jwtConfig.getExpiration() / 1000));
    }

    private ResponseCookie buildAdministratorRefreshTokenCookie(String token) {
        return buildCookie("administrator_refresh", token, jwtConfig.getRefreshExpiration() / 1000);
    }

    private ResponseCookie deleteCookie(String name) {
        return buildCookie(name, "", 0);
    }

    private ResponseCookie buildCookie(String name, String value, long maxAgeSeconds) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(jwtConfig.isCookieSecure())
                .path("/")
                .sameSite(jwtConfig.getCookieSameSite())
                .maxAge(maxAgeSeconds)
                .build();
    }

    @PostMapping("/access-code/generate")
    @AuditableAction(action = "GENERATE_ACCESS_CODE", entity = "STUDENT_ACCESS_CODE")
    public ResponseEntity<StudentAccessCodeResponse> generateAccessCode(@Valid @RequestBody StudentAccessCodeRequest request) {
        StudentAccessCodeResponse response = studentAuthenticationService.generateAccessCodeResponse(
                request.getStudentId(),
                request.getPassword()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/student/login")
    @AuditableAction(action = "AUTHENTICATE_STUDENT", entity = "STUDENT_AUTHENTICATION")
    public ResponseEntity<?> studentLogin(@Valid @RequestBody StudentLoginRequest loginRequest, HttpServletRequest request) {

        var response = studentAuthenticationService.authenticateWithAccessCode(
                loginRequest.getStudentId(),
                loginRequest.getAccessCode(),
                request
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildStudentAccessTokenCookie(response.getAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildStudentRefreshTokenCookie(response.getRefreshToken()).toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("supervisor_access").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("supervisor_refresh").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("administrator_access").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("administrator_refresh").toString())
                .body(Map.of("message", "Authentication successful", "studentId", response.getStudentId()));
    }

    @PostMapping("/supervisor/login")
    @AuditableAction(action = "AUTHENTICATE_SUPERVISOR", entity = "SUPERVISOR_AUTHENTICATION")
    public ResponseEntity<?> supervisorLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        var response = supervisorAccountsAuthenticationService.login(loginRequest, request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildSupervisorAccessTokenCookie(response.getAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildSupervisorRefreshTokenCookie(response.getRefreshToken()).toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("administrator_access").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("administrator_refresh").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("student_access").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("student_refresh").toString())
                .body(Map.of("message", "Authentication successful", "evaluatorId", response.getEvaluatorId(), "college", response.getCollege(), "program", response.getPrograms(), "requiresPasswordChange",
                response.getRequiresPasswordChange()));
    }

    @PostMapping("/administrator/login")
    @AuditableAction(action = "AUTHENTICATE_ADMINISTRATOR", entity = "ADMINISTRATOR_AUTHENTICATION")
    public ResponseEntity<?> administratorLogin(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        var response = administratorAccountsAuthenticationService.login(loginRequest, request);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildAdministratorAccessTokenCookie(response.getAccessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, buildAdministratorRefreshTokenCookie(response.getRefreshToken()).toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("supervisor_access").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("supervisor_refresh").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("student_access").toString())
                .header(HttpHeaders.SET_COOKIE, deleteCookie("student_refresh").toString())
                .body(Map.of("message", "Authentication successful", "administratorId", response.getEvaluatorId(), "role", response.getRole()));
    }

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> getAuthConfig() {
        return ResponseEntity.ok(Map.of("googleClientId", googleTokenVerifierService.getGoogleClientId() != null ? googleTokenVerifierService.getGoogleClientId() : ""));
    }

    @PostMapping("/google")
    @AuditableAction(action = "AUTHENTICATE_GOOGLE", entity = "GOOGLE_AUTHENTICATION")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request, HttpServletRequest httpRequest) {
        var result = googleAuthenticationService.authenticate(request, httpRequest);
        UserAccounts user = result.user();

        ResponseCookie accessCookie;
        ResponseCookie refreshCookie;

        if (result.isAdminRole()) {
            accessCookie = buildAdministratorAccessTokenCookie(result.accessToken());
            refreshCookie = buildAdministratorRefreshTokenCookie(result.refreshToken());
        } else {
            accessCookie = buildSupervisorAccessTokenCookie(result.accessToken());
            refreshCookie = buildSupervisorRefreshTokenCookie(result.refreshToken());
        }

        var responseBuilder = ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        if (result.isAdminRole()) {
            responseBuilder
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("supervisor_access").toString())
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("supervisor_refresh").toString())
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("student_access").toString())
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("student_refresh").toString());
        } else {
            responseBuilder
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("administrator_access").toString())
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("administrator_refresh").toString())
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("student_access").toString())
                    .header(HttpHeaders.SET_COOKIE, deleteCookie("student_refresh").toString());
        }

        return responseBuilder.body(Map.of(
                "message", "Authentication successful",
                "userId", user.getUserId().toString(),
                "evaluatorId", user.getUserId().toString(),
                "email", user.getEmail(),
                "role", "ROLE_" + user.getRole().name(),
                "college", user.getCollege() != null ? user.getCollege().name() : "",
                "program", user.getPrograms() != null ? user.getPrograms().name() : "",
                "requiresPasswordChange", user.getPasswordChangedAt() == null
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String role = authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse("ROLE_STUDENT");
        String principal = authentication.getName();

        if (role.equals("ROLE_STUDENT")) {
            return ResponseEntity.ok(CurrentUserResponse.builder()
                    .authenticated(true)
                    .studentId(principal)
                    .userId(principal)
                    .role(role)
                    .build());
        }

        Long userId = Long.parseLong(principal);

        if (role.equals("ROLE_DEAN") || role.equals("ROLE_PROGRAM_CHAIR")) {
            return ResponseEntity.ok(supervisorAccountsAuthenticationService.getSupervisorProfile(userId, role));
        }

        return ResponseEntity.ok(CurrentUserResponse.builder()
                .authenticated(true)
                .userId(String.valueOf(userId))
                .administratorId(String.valueOf(userId))
                .role(role)
                .build());
    }

    @GetMapping("/csrf")
    public ResponseEntity<?> csrf(CsrfToken csrfToken) {
        return ResponseEntity.ok(Map.of(
                "headerName", csrfToken.getHeaderName(),
                "parameterName", csrfToken.getParameterName(),
                "token", csrfToken.getToken()
        ));
    }
    @PutMapping("/change-password")
    @AuditableAction(action = "CHANGE_PASSWORD", entity = "USER_ACCOUNTS")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        Long userId = Long.parseLong(
                authentication.getPrincipal().toString()
        );
        supervisorAccountsAuthenticationService
                .changePassword(userId, request);
        return ResponseEntity.ok(
                "Password changed successfully");
    }
    @PostMapping("/logout")
    @AuditableAction(action = "LOGOUT", entity = "USER_LOGOUT")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = extractCookie(request, "student_refresh");
            if (refreshToken == null) {
                refreshToken = extractCookie(request, "supervisor_refresh");
            }
            if (refreshToken == null) {
                refreshToken = extractCookie(request, "administrator_refresh");
            }
            if (refreshToken != null) {
                String hash = TokenHashUtil.sha256(refreshToken);
                refreshTokenRepository.findByTokenHashAndRevokedFalse(hash).ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
            }
        } catch (Exception e) {

            log.warn("Logout token revoke failed: {}", e.getMessage());

        }
        Cache cache = cacheManager.getCache("facultyClasses");
        if (cache != null) {
            cache.clear();
        }
        request.getSession().invalidate();
        return ResponseEntity.ok().header("Set-Cookie", deleteCookie("student_access").toString()).header("Set-Cookie", deleteCookie("student_refresh").toString()).header("Set-Cookie", deleteCookie("supervisor_access").toString()).header("Set-Cookie", deleteCookie("supervisor_refresh").toString()).header("Set-Cookie", deleteCookie("administrator_access").toString()).header("Set-Cookie", deleteCookie("administrator_refresh").toString()).body(Map.of("message", "Logged out"));
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }
}
