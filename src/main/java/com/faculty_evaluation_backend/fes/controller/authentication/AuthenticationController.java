package com.faculty_evaluation_backend.fes.controller.authentication;

import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.dto.authentication.LoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.repositories.tokens.RefreshTokenRepository;
import com.faculty_evaluation_backend.fes.services.authentication.AdministratorAccountsAuthenticationService;
import com.faculty_evaluation_backend.fes.services.authentication.StudentAuthenticationService;
import com.faculty_evaluation_backend.fes.services.authentication.SupervisorAccountsAuthenticationService;
import com.faculty_evaluation_backend.fes.utilities.token.TokenHashUtil;
import org.springframework.cache.Cache;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CacheManager cacheManager;

    private ResponseCookie buildStudentAccessTokenCookie(String token) {
        return ResponseCookie.from("student_access", token).httpOnly(true).secure(true).path("/").sameSite("None").maxAge(Math.max(1, jwtConfig.getExpiration() / 1000)).build();
    }

    private ResponseCookie buildStudentRefreshTokenCookie(String token) {
        return ResponseCookie.from("student_refresh", token).httpOnly(true).secure(true).path("/").sameSite("None").maxAge(jwtConfig.getRefreshExpiration() / 1000).build();
    }

    private ResponseCookie buildSupervisorAccessTokenCookie(String token) {
        return ResponseCookie.from("supervisor_access", token).httpOnly(true).secure(true).path("/").sameSite("None").maxAge(Math.max(1, jwtConfig.getExpiration() / 1000)).build();
    }

    private ResponseCookie buildSupervisorRefreshTokenCookie(String token) {
        return ResponseCookie.from("supervisor_refresh", token).httpOnly(true).secure(true).path("/").sameSite("None").maxAge(Math.max(1, jwtConfig.getRefreshExpiration() / 1000)).build();
    }

    private ResponseCookie buildAdministratorAccessTokenCookie(String token) {
        return ResponseCookie.from("administrator_access", token).httpOnly(true).secure(true).path("/").sameSite("None").maxAge(Math.max(1, jwtConfig.getExpiration() / 1000)).build();
    }

    private ResponseCookie buildAdministratorRefreshTokenCookie(String token) {
        return ResponseCookie.from("administrator_refresh", token).httpOnly(true).secure(true).path("/").sameSite("None").maxAge(jwtConfig.getRefreshExpiration() / 1000).build();
    }

    private ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "").httpOnly(true).secure(true).path("/").sameSite("None").maxAge(0).build();
    }

    @PostMapping("/access-code/generate")
    public ResponseEntity<?> generateAccessCode(@RequestParam String studentId, @RequestParam String password) {
        StudentAccessCode code = studentAuthenticationService.generateAccessCode(studentId, password);
        return ResponseEntity.ok(Map.of("studentId", studentId, "accessCode", code.getAccessCode(), "expiresAt", code.getExpiresAt()));
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(@RequestParam String studentId, @RequestParam String accessCode, HttpServletRequest request) {

        var response = studentAuthenticationService.authenticateWithAccessCode(studentId, accessCode, request);

        return ResponseEntity.ok().header("Set-Cookie", buildStudentAccessTokenCookie(response.getAccessToken()).toString()).header("Set-Cookie", buildStudentRefreshTokenCookie(response.getRefreshToken()).toString()).body(Map.of("message", "Authentication successful", "studentId", response.getStudentId(), "accessCode", response.getAccessCode()));
    }

    @PostMapping("/supervisor/login")
    public ResponseEntity<?> supervisorLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        var response = supervisorAccountsAuthenticationService.login(loginRequest);
        return ResponseEntity.ok().header("Set-Cookie", buildSupervisorAccessTokenCookie(response.getAccessToken()).toString()).header("Set-Cookie", buildSupervisorRefreshTokenCookie(response.getRefreshToken()).toString()).body(Map.of("message", "Authentication successful", "evaluatorId", response.getEvaluatorId(), "college", response.getCollege(), "program", response.getPrograms()));
    }

    @PostMapping("/administrator/login")
    public ResponseEntity<?> administratorLogin(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        var response = administratorAccountsAuthenticationService.login(loginRequest);

        return ResponseEntity.ok().header("Set-Cookie", buildAdministratorAccessTokenCookie(response.getAccessToken()).toString()).header("Set-Cookie", buildAdministratorRefreshTokenCookie(response.getRefreshToken()).toString()).body(Map.of("message", "Authentication successful", "administratorId", response.getEvaluatorId()));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String role = authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse("ROLE_STUDENT");

        Long userId = Long.parseLong(authentication.getName());

        if (role.equals("ROLE_DEAN") || role.equals("ROLE_PROGRAM_CHAIR")) {

            UserAccounts user = supervisorAccountsAuthenticationService.findByUserId(userId);

            return ResponseEntity.ok(Map.of("authenticated", true, "userId", user.getUserId(), "role", role, "college", user.getCollege(), "program", user.getPrograms()));
        }

        return ResponseEntity.ok(Map.of("authenticated", true, "userId", userId, "role", role));
    }

    @PostMapping("/logout")
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

        /*
         * CLEAR SPRING CACHE
         */

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