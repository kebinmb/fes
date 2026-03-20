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
                String studentID = claims.getSubject();
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        studentID,
                        null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"))
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("Authenticated student: {}",studentID);
            }
        }catch (UnauthorizedException e){
            log.error("Authentication failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request,response);
    }

    private String extractAccessToken(HttpServletRequest request){
        if(request.getCookies() == null){
            return null;
        }
        for (Cookie cookie : request.getCookies()){
            if("student_access".equals(cookie.getName())){
                return cookie.getValue();
            }
        }
        return null;
    }
}
