package com.faculty_evaluation_backend.fes.services.authentication;

import com.faculty_evaluation_backend.fes.audit.AuditableAction;
import com.faculty_evaluation_backend.fes.dto.authentication.AuthenticationResponse;
import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import com.faculty_evaluation_backend.fes.exceptions.BadRequestException;
import com.faculty_evaluation_backend.fes.exceptions.ResourceNotFoundException;
import com.faculty_evaluation_backend.fes.exceptions.UnauthorizedException;
import com.faculty_evaluation_backend.fes.repositories.authentication.StudentAccessCodeRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import com.faculty_evaluation_backend.fes.services.cache.StudentCacheService;
import com.faculty_evaluation_backend.fes.services.jwt.JwtService;
import com.faculty_evaluation_backend.fes.services.rateLimiting.RateLimitingService;
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
    private static final int ACCESS_CODE_EXPIRY_MINUTES = 15;

    @Transactional(transactionManager = "primaryTransactionManager")
    @AuditableAction(action = "GENERATE_ACCESS_CODE", entity = "STUDENT_ACCESS_CODE")
    public StudentAccessCode generateAccessCode(String studentId){
        rateLimitingService.consume(studentId,"GENERATE_ACCESS_CODE");
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
            return existingCode.get();
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
    public AuthenticationResponse authenticateWithAccessCode(String studentId, String accessCode){
        rateLimitingService.consume(studentId,"AUTHENTICATE");
        int updated = studentAccessCodeRepository
                .markAsUsedIfValid(studentId, accessCode);

        if(updated == 0){
            throw new UnauthorizedException("Invalid, expired, or already used access code");
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        studentCacheService.evictStudent(studentId);
                    }
                }
        );
        String accessToken =
                jwtService.generateAccessTokenForStudent(studentId, accessCode);

        String refreshToken =
                jwtService.generateRefreshTokenForStudent(studentId);

        log.info("Student {} authenticated", studentId);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(900L)
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