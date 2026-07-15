package com.faculty_evaluation_backend.fes.services.cache;

import com.faculty_evaluation_backend.fes.entities.authentication.StudentAccessCode;
import com.faculty_evaluation_backend.fes.repositories.authentication.StudentAccessCodeRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentLoadRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentCacheService {
    private final PrimaryStudentRepository primaryStudentRepository;
    private final PrimaryStudentLoadRepository primaryStudentLoadRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;
    private final StudentAccessCodeRepository studentAccessCodeRepository;

    @Cacheable(value = "studentExists", key = "#studentId")
    public boolean studentExists(String studentId){
        return primaryStudentRepository.existsByLegacyId(studentId);
    }
    @Cacheable(value = "studentLoadCount", key = "#studentId")
    public int getTotalLoad(String studentId){
        return Optional.ofNullable(
                primaryStudentLoadRepository.countDistinctTotalPrimaryStudentLoadByStudentId(studentId)
        ).orElse(0);
    }
    @Cacheable(value = "evaluatedCount", key = "#studentId")
    public int getEvaluatedCount(String studentId){
        return Optional.ofNullable(facultyEvaluationScoreRepository.countDistinctEvaluatedSubjectsByEvaluatorId(studentId)
        ).orElse(0);
    }
    @Cacheable(value = "activeAccessCode", key = "#studentId")
    public Optional<StudentAccessCode> getActiveAccessCode(String studentId){
        return studentAccessCodeRepository.findLatestValidAccessCode(studentId, Instant.now());
    }
    @CacheEvict(value = {"activeAccessCode", "evaluatedCount"}, key = "#studentId")
    public void evictStudent(String studentId){

    }
}
