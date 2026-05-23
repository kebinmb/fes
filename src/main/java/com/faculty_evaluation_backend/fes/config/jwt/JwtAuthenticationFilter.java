package com.faculty_evaluation_backend.fes.config.jwt;

import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.services.authentication.StudentAuthenticationService;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        log.info("JWT FILTER -> {}", request.getServletPath());

        try {

            String token = extractAccessToken(request);

            log.info("TOKEN EXISTS: {}", token != null);

            if (token != null) {

                log.info("TOKEN VALUE: {}", token);

                boolean valid =
                        jwtService.isAccessTokenValid(token);

                boolean expired =
                        jwtService.isTokenExpired(token);

                log.info("TOKEN VALID: {}", valid);

                log.info("TOKEN EXPIRED: {}", expired);

                if (valid && !expired) {

                    Claims claims =
                            jwtService.extractAllClaims(token);

                    String userId =
                            claims.getSubject();

                    String role =
                            claims.get("role", String.class);

                    log.info("USER ID: {}", userId);

                    log.info("ROLE: {}", role);

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    Collections.singletonList(
                                            new SimpleGrantedAuthority(role)
                                    )
                            );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(authenticationToken);

                    log.info("AUTHENTICATION SET");
                }
            }

        } catch (Exception e) {

            log.error("JWT FILTER ERROR", e);

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getServletPath();

        return path.equals("/auth/student/login") ||
                path.equals("/auth/supervisor/login") ||
                path.equals("/auth/access-code/generate") || path.equals("/auth/administrator/login");
    }
    private String extractAccessToken(HttpServletRequest request) {

        String supervisorToken = null;
        String studentToken = null;
        String administratorToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {

                if ("supervisor_access".equals(cookie.getName())) {
                    supervisorToken = cookie.getValue();

                } else if ("student_access".equals(cookie.getName())) {
                    studentToken = cookie.getValue();

                } else if ("administrator_access".equals(cookie.getName())) {
                    administratorToken = cookie.getValue();
                }
            }
        }

        String path = request.getServletPath();

        if (path.contains("/student") && studentToken != null) {
            return studentToken;
        }

        if (path.contains("/supervisor") && supervisorToken != null) {
            return supervisorToken;
        }

        if (path.contains("/admin") && administratorToken != null) {
            return administratorToken;
        }

        // fallback priority
        if (administratorToken != null) return administratorToken;
        if (supervisorToken != null) return supervisorToken;
        if (studentToken != null) return studentToken;

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
