package com.faculty_evaluation_backend.fes.controller;

import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.services.authentication.StudentAuthenticationService;
import com.faculty_evaluation_backend.fes.services.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthenticationController {
    private final StudentAuthenticationService studentAuthenticationService;
    private final JwtService jwtService;
    private final JwtConfig jwtConfig;


    private ResponseCookie buildStudentAccessTokenCookie(String token, long maxAge){
        return ResponseCookie.from("access_token",token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie buildStudentRefreshTokenCookie(String token){
        return ResponseCookie.from("refresh_token", token)
                .httpOnly(true)
                .secure(false)
                .path("/auth/student/refresh")
                .sameSite("Strict")
                .maxAge(7 * 24 * 60 * 60)
                .build();
    }

    @PostMapping("/access-code/generate")
    public ResponseEntity<?> generateAccessCode(@RequestParam String studentId){
        StudentAccessCode code = studentAuthenticationService.generateAccessCode(studentId);
        return ResponseEntity.ok().body(
                Map.of(
                        "studentId", studentId,
                        "accessCode", code.getAccessCode(),
                        "expiresAt", code.getExpiresAt()
                )
        );
    }

    @PostMapping("/student/login")
    public ResponseEntity<?> studentLogin(
            @RequestParam String studentId,
            @RequestParam String accessCode
    ){
        var response = studentAuthenticationService.authenticateWithAccessCode(studentId, accessCode);
        ResponseCookie accessTokenCookie = buildStudentAccessTokenCookie(response.getAccessToken(), response.getExpiresIn());
        ResponseCookie refreshTokenCookie = buildStudentRefreshTokenCookie(response.getRefreshToken());
        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(Map.of(
                        "message", "Authentication successful",
                        "studentId", response.getStudentId()
                ));
    }

    @PostMapping("/student/refresh")
    public ResponseEntity<?> studentRefreshToken(@CookieValue(value = "refresh_token", required = false)String refreshToken,
                                                 HttpServletResponse response){
        if (refreshToken == null || !jwtService.isRefreshTokenValid(refreshToken)){
            throw new UnauthorizedException("Invalid refresh token.");
        }
        String studentId = jwtService.extractStudentId(refreshToken);
        String newAccessToken = jwtService.generateAccessTokenForStudent(studentId,null);
        ResponseCookie newAccessCookie = buildStudentAccessTokenCookie(newAccessToken, jwtConfig.getExpiration());
        return ResponseEntity.ok()
                .header("Set-Cookie", newAccessCookie.toString())
                .body(Map.of("message", "Token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie deleteAccess = ResponseCookie.from("access_token", "")
                .maxAge(0)
                .path("/")
                .build();

        ResponseCookie deleteRefresh = ResponseCookie.from("refresh_token", "")
                .maxAge(0)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", deleteAccess.toString())
                .header("Set-Cookie", deleteRefresh.toString())
                .body(Map.of("message", "Logged out"));
    }

}
