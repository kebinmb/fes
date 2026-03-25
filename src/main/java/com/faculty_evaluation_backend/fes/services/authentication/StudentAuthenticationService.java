package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.config.jwt.JwtConfig;
import com.faculty_evaluation_backend.fes.dto.authentication.AuthenticationResponse;
import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import com.faculty_evaluation_backend.fes.entities.tokens.RefreshToken;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.authentication.StudentAccessCodeRepository;
import com.faculty_evaluation_backend.fes.repositories.tokens.RefreshTokenRepository;
import com.faculty_evaluation_backend.fes.services.cache.StudentCacheService;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.services.rateLimiting.RateLimitingService;
import com.faculty_evaluation_backend.fes.utilities.token.TokenHashUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class StudentAuthenticationService {

    private final StudentAccessCodeRepository studentAccessCodeRepository;
    private final JwtService jwtService;
    private final StudentCacheService studentCacheService;
    private final RateLimitingService rateLimitingService;
    private final JwtConfig jwtConfig;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final int ACCESS_CODE_EXPIRY_MINUTES = 15;

    @Transactional(transactionManager = "primaryTransactionManager")
    @AuditableAction(action = "GENERATE_ACCESS_CODE", entity = "STUDENT_ACCESS_CODE")
    public StudentAccessCode generateAccessCode(String studentId){
        rateLimitingService.consumeStudentRequest(studentId,"GENERATE_ACCESS_CODE");
        if(!studentCacheService.studentExists(studentId)){
            throw new ResourceNotFoundException("Student ID not found: " + studentId);
        }
        if(studentAccessCodeRepository.existsByStudentIdAndIsCompletedTrue(studentId)){
            throw new BadRequestException("You already finished evaluating.");
        }
        int totalLoad = studentCacheService.getTotalLoad(studentId);
        int totalEvaluated = studentCacheService.getEvaluatedCount(studentId);


        if(totalLoad > 0 && totalLoad == totalEvaluated){
            throw new BadRequestException("You already finished evaluating all your subjects.");
        }
        Optional<StudentAccessCode> existingCode =
                studentAccessCodeRepository
                        .findLatestValidAccessCodeForUpdate(studentId, Instant.now());

        if(existingCode.isPresent()){
            StudentAccessCode code = existingCode.get();

            if (code.isValid()) {
                return code;
            }
        }
        String accessCode = generateAccessCodeSecure();

        StudentAccessCode newAccessCode = StudentAccessCode.builder()
                .studentId(studentId)
                .accessCode(accessCode)
                .expiresAt(Instant.now().plusSeconds(ACCESS_CODE_EXPIRY_MINUTES * 60L))
                .isUsed(false)
                .isCompleted(false)
                .build();

        studentAccessCodeRepository.save(newAccessCode);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        studentCacheService.evictStudent(studentId);
                    }
                }
        );
        log.info("Generated access code for student {}", studentId);
        return newAccessCode;
    }

    @AuditableAction(action = "AUTHENTICATE_STUDENT", entity = "STUDENT_AUTHENTICATION")
    @Transactional(transactionManager = "primaryTransactionManager")
    public AuthenticationResponse authenticateWithAccessCode(String studentId, String accessCode, HttpServletRequest request){
        rateLimitingService.consumeStudentRequest(studentId,"AUTHENTICATE_STUDENT");
        String normalizedAccessCode  = accessCode.trim().toUpperCase();
        StudentAccessCode code = studentAccessCodeRepository.findForUpdate(studentId, normalizedAccessCode )
                        .orElseThrow(() -> new UnauthorizedException("Invalid access code"));
        if (!code.isValid()) {
            throw new UnauthorizedException("Invalid, expired, or already used access code");
        }
        code.markAsUsed();
        studentAccessCodeRepository.save(code);
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        studentCacheService.evictStudent(studentId);
                    }
                }
        );
        String accessToken =
                jwtService.generateAccessTokenForStudent(studentId);

        String refreshToken =
                jwtService.generateRefreshTokenForStudent(studentId);
        String tokenHash = TokenHashUtil.sha256(refreshToken);
        String rawDevice = request.getHeader("User-Agent");
        String deviceInfo = TokenHashUtil.sha256(rawDevice != null ? rawDevice : "unknown").substring(0,16);
        refreshTokenRepository.revokeAllByUserId(studentId);
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(studentId)
                        .tokenHash(tokenHash)
                        .expiryDate(Instant.now().plusMillis(jwtConfig.getRefreshExpiration()))
                        .revoked(false)
                        .deviceInfo(deviceInfo)
                        .build()
        );
        log.info("Student {} authenticated", studentId);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtConfig.getExpiration())
                .studentId(studentId)
                .build();
    }

    private static final String CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final SecureRandom random = new SecureRandom();

    private String generateAccessCodeSecure(){
        StringBuilder code = new StringBuilder(12);
        for(int i = 0; i < 12; i++){
            code.append(CHARSET.charAt(random.nextInt(CHARSET.length())));
        }
        return code.toString();
    }
}