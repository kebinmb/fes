package com.faculty_evaluation_backend.fes.services.jwt;

import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final JwtConfig jwtConfig;
    private SecretKey key;
    private JwtParser parser;
    private final UserAccountsRepository userAccountsRepository;

    @PostConstruct
    public void init(){
        byte[] keyBytes;
        try{
            keyBytes = Base64.getDecoder().decode(jwtConfig.getSecret());
        }catch (IllegalArgumentException e){
            keyBytes = jwtConfig.getSecret().getBytes();
        }
        if(keyBytes.length < 32){
            throw new IllegalStateException("JWT Secret must be at least 256 bits (32 bytes)");
        }
        key = Keys.hmacShaKeyFor(keyBytes);
        parser = Jwts.parser().verifyWith(key).build();
    }

    public String generateAccessTokenForStudent(String studentId){
        Map<String,Object> claims = new HashMap<>();
        claims.put("type", "student_access");
        return createToken(claims, studentId, jwtConfig.getExpiration());
    }
    public String generateRefreshTokenForStudent(String studentId){
        Map<String, Object> claims = new HashMap<>();
        claims.put("studentId", studentId);
        claims.put("type", "student_refresh");
        claims.put("role", "ROLE_STUDENT");
        return createToken(claims, studentId, jwtConfig.getRefreshExpiration());
    }

    public String generateAccessTokenForSupervisor(Long userId){
        UserAccounts user = userAccountsRepository.findUserByUserId(userId)
                .orElseThrow(()-> new IllegalStateException("User detais not found for user ID = " + userId));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "supervisor_access");
        claims.put("role","ROLE_"+user.getRole().name());
        claims.put("college", user.getCollege().name());
        claims.put("semester","1st");
        claims.put("schoolYear",2023);
        return createToken(claims,userId.toString(), jwtConfig.getExpiration());
    }
    public String generateRefreshTokenForSupervisor(Long userId){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "supervisor_refresh");
        return createToken(claims, userId.toString(), jwtConfig.getRefreshExpiration());
    }
    public String extractStudentId(String token){
        return extractClaim(token, Claims::getSubject);
    }
    public String extractUserId(String token){
        return extractClaim(token, Claims::getSubject);
    }
    public boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
    public String extractTokenType(String token){
        return extractClaim(token, claims -> claims.get("type", String.class));
    }
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    public Claims extractAllClaims(String token){
        return parser
                .parseSignedClaims(token).getPayload();
    }
    private String createToken(Map<String, Object> claims, String subject, Long expiration){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer("faculty-evaluation-system")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean isAccessTokenValid(String token){
        try{
            Claims claims = parser.parseSignedClaims(token).getPayload();
            if (!"faculty-evaluation-system".equals(claims.getIssuer())) {
                throw new RuntimeException("Invalid issuer");
            }
            String type = claims.get("type", String.class);
            return ("student_access".equals(type) || "supervisor_access".equals(type)) && claims.getExpiration().after(new Date()) && claims.getSubject() != null;
        }catch (Exception e){
            log.warn("Invalid access token: {}", e.getMessage());
            return false;
        }
    }
    public boolean isRefreshTokenValid(String token){
        try{
            Claims claims = parser.parseSignedClaims(token).getPayload();
            if (!"faculty-evaluation-system".equals(claims.getIssuer())) {
                throw new RuntimeException("Invalid issuer");
            }
            String type = claims.get("type", String.class);
            return ("student_refresh".equals(type) || "faculty_refresh".equals(type)) && claims.getExpiration().after(new Date()) && claims.getSubject() != null;
        }catch (Exception e){
            log.warn("Invalid refresh token: {}", e.getMessage());
            return false;
        }
    }
}
