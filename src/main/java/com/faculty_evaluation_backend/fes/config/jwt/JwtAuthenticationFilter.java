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
    private final StudentAuthenticationService studentAuthenticationService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException{
        try{
            String token = extractAccessToken(request);
            if (token != null
                    && jwtService.isAccessTokenValid(token)
                    && !jwtService.isTokenExpired(token)) {
                Claims claims = jwtService.extractAllClaims(token);
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority(role))
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("Authenticated {} with role {}", userId, role);
            }
        }catch (UnauthorizedException e){
            log.warn("JWT auth failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request,response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getServletPath();

        return path.equals("/auth/student/login") ||
                path.equals("/auth/supervisor/login") ||
                path.equals("/auth/access-code/generate");
    }
    private String extractAccessToken(HttpServletRequest request) {
        String supervisorToken = null;
        String studentToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("supervisor_access".equals(cookie.getName())) {
                    supervisorToken = cookie.getValue();
                } else if ("student_access".equals(cookie.getName())) {
                    studentToken = cookie.getValue();
                }
            }
        }

        String path = request.getServletPath();

        // ✅ FIXED: use contains instead of startsWith
        if (path.contains("/student")) {
            return studentToken;
        }

        if (path.contains("/supervisor")) {
            return supervisorToken;
        }

        // fallback
        if (supervisorToken != null) return supervisorToken;
        if (studentToken != null) return studentToken;

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
