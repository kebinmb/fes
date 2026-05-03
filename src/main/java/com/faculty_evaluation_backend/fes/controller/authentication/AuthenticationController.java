package com.faculty_evaluation_backend.fes.controller.authentication;

import com.faculty_evaluation_backend.fes.dto.authentication.LoginRequest;
import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import com.faculty_evaluation_backend.fes.repositories.tokens.RefreshTokenRepository;
import com.faculty_evaluation_backend.fes.services.authentication.StudentAuthenticationService;
import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.services.authentication.SupervisorAccountsAuthenticationService;
import com.faculty_evaluation_backend.fes.utilities.token.TokenHashUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;

    private ResponseCookie buildStudentAccessTokenCookie(String token) {
        return ResponseCookie.from("student_access", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Math.max(1, jwtConfig.getExpiration() / 1000))
                .build();
    }

    private ResponseCookie buildStudentRefreshTokenCookie(String token) {
        return ResponseCookie.from("student_refresh", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(jwtConfig.getRefreshExpiration() / 1000)
                .build();
    }
    private ResponseCookie buildSupervisorAccessTokenCookie(String token){
        return ResponseCookie.from("supervisor_access",token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Math.max(1, jwtConfig.getExpiration() / 1000))
                .build();
    }
    private ResponseCookie buildSupervisorRefreshTokenCookie(String token){
        return ResponseCookie.from("supervisor_refresh",token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Math.max(1, jwtConfig.getRefreshExpiration()/1000))
                .build();
    }
    private ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(0)
                .build();
    }
    @PostMapping("/access-code/generate")
    public ResponseEntity<?> generateAccessCode(@RequestParam String studentId) {
        StudentAccessCode code = studentAuthenticationService.generateAccessCode(studentId);

        return ResponseEntity.ok(Map.of(
                "studentId", studentId,
                "accessCode", code.getAccessCode(),
                "expiresAt", code.getExpiresAt()
        ));
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(
            @RequestParam String studentId,
            @RequestParam String accessCode,
            HttpServletRequest request
    ) {

        var response = studentAuthenticationService
                .authenticateWithAccessCode(studentId, accessCode, request);

        return ResponseEntity.ok()
                .header("Set-Cookie", buildStudentAccessTokenCookie(response.getAccessToken()).toString())
                .header("Set-Cookie", buildStudentRefreshTokenCookie(response.getRefreshToken()).toString())
                .body(Map.of(
                        "message", "Authentication successful",
                        "studentId", response.getStudentId(),
                        "accessCode",response.getAccessCode()
                ));
    }
    @PostMapping("/supervisor/login")
    public ResponseEntity<?> supervisorLogin(
            @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ){
        var response = supervisorAccountsAuthenticationService.login(loginRequest);
        return ResponseEntity.ok()
                .header("Set-Cookie", buildSupervisorAccessTokenCookie(response.getAccessToken()).toString())
                .header("Set-Cookie", buildSupervisorRefreshTokenCookie(response.getRefreshToken()).toString())
                .body(Map.of("message","Authentication successful","evaluatorId",response.getEvaluatorId(),"college",response.getCollege()));
    }
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(Map.of(
                "userId", authentication.getName(),
                "role", authentication.getAuthorities()
                        .stream()
                        .findFirst()
                        .map(Object::toString)
                        .orElse("ROLE_STUDENT")
        ));
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        try {
            String refreshToken = extractCookie(request, "student_refresh");
            if(refreshToken == null){
                refreshToken = extractCookie(request, "supervisor_refresh");
            }
            if (refreshToken != null) {
                String hash = TokenHashUtil.sha256(refreshToken);

                refreshTokenRepository.findByTokenHashAndRevokedFalse(hash)
                        .ifPresent(token -> {
                            token.setRevoked(true);
                            refreshTokenRepository.save(token);
                        });
            }

        } catch (Exception e) {
            log.warn("Logout token revoke failed: {}", e.getMessage());
        }

        return ResponseEntity.ok()
                .header("Set-Cookie", deleteCookie("student_access").toString())
                .header("Set-Cookie", deleteCookie("student_refresh").toString())
                .header("Set-Cookie", deleteCookie("supervisor_access").toString())
                .header("Set-Cookie", deleteCookie("supervisor_refresh").toString())
                .body(Map.of("message", "Logged out"));
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