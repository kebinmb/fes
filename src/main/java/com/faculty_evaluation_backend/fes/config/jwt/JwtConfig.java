package com.faculty_evaluation_backend.fes.config.jwt;

import lombok.Getter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;
    @Value("${jwt.idle-timeout:600000}")
    private Long idleTimeout;
    @Value("${jwt.cookie-secure:true}")
    private boolean cookieSecure;
    @Value("${jwt.cookie-same-site:None}")
    private String cookieSameSite;
}
