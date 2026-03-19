package com.faculty_evaluation_backend.fes.services.jwt;

import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryFacultyRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
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
    private final PrimaryStudentRepository primaryStudentRepository;
    private final PrimaryFacultyRepository primaryFacultyRepository;

    @PostConstruct
    public void init(){
        key = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes());
        parser = Jwts.parser().verifyWith(key).build();
    }

    public String generateAccessTokenForStudent(String studentId, String accessCode){
        Map<String,Object> claims = new HashMap<>();
        claims.put("studentId", studentId);
        claims.put("accessCode", accessCode);
        claims.put("type", "access");
        return createToken(claims, studentId, jwtConfig.getExpiration());
    }
    public String generateRefreshTokenForStudent(String studentId){
        Map<String, Object> claims = new HashMap<>();
        claims.put("studentId", studentId);
        claims.put("type", "student_refresh");
        return createToken(claims, studentId, jwtConfig.getRefreshExpiration());
    }

    public String generateAccessTokenForFaculty(Long userId){
        UserAccounts user = userAccountsRepository.findUserByUserId(userId)
                .orElseThrow(()-> new IllegalStateException("User detais not found for user ID = " + userId));
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "faculty");
        claims.put("role", user.getRole().name());
        claims.put("college", user.getCollege().name());
        claims.put("semester","1st");
        claims.put("schoolYear",2023);
        return createToken(claims,userId.toString(), jwtConfig.getExpiration());
    }
    public String generateRefreshTokenForFaculty(Long userId){
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "faculty_refresh");
        return createToken(claims, userId.toString(), jwtConfig.getExpiration());
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
    private Claims extractAllClaims(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private String createToken(Map<String, Object> claims, String subject, Long expiration){
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public boolean isRefreshTokenValid(String token){
        try{
            var claims = parser.parseSignedClaims(token).getPayload();
            return "student_refresh".equals(claims.get("type", String.class)) && claims.getExpiration().after(new Date());
        }catch (Exception e){
            return false;
        }
    }
}
