package com.faculty_evaluation_backend.fes.services.data.evaluation.strategies;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.entities.primary.PrimaryStudent;
import com.faculty_evaluation_backend.fes.repositories.authentication.StudentAccessCodeRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentEvaluationStrategy implements EvaluationStrategy {
    private final PrimaryStudentRepository primaryStudentRepository;
    private final StudentAccessCodeRepository studentAccessCodeRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    @Override
    public void validate(BaseEvaluationDTO baseEvaluationDTO){
        PrimaryStudent student = primaryStudentRepository.findByStudentId(baseEvaluationDTO.getEvaluatorId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        studentAccessCodeRepository.findByStudentIdAndAccessCode(baseEvaluationDTO.getEvaluatorId(), baseEvaluationDTO.getAccessCode())
                .orElseThrow(() -> new RuntimeException("Invalid access code"));

        validateCurrentPrincipal(student);
    }

    private void validateCurrentPrincipal(PrimaryStudent student) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return;
        }

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        if (isAdmin) {
            return;
        }

        String principalName = auth.getName();
        if (principalName == null || principalName.isBlank()) {
            throw new AccessDeniedException("Unauthorized evaluator identity");
        }

        boolean matchesStudentId = principalName.equalsIgnoreCase(student.getStudentId());
        boolean matchesLegacyId = student.getLegacyId() != null && principalName.equalsIgnoreCase(student.getLegacyId());

        if (!matchesStudentId && !matchesLegacyId) {
            throw new AccessDeniedException("You are not authorized to submit evaluations for another student.");
        }
    }

    @Override
    public boolean isDuplicate(BaseEvaluationDTO baseEvaluationDTO){
        return facultyEvaluationScoreRepository.existsByFacultyIdAndEvaluatorIdAndClassCodeAndSubjectCodeAndYearLevelAndSemesterAndSchoolYear(
                baseEvaluationDTO.getFacultyId(),
                baseEvaluationDTO.getEvaluatorId(),
                baseEvaluationDTO.getClassCode(),
                baseEvaluationDTO.getSubjectCode(),
                baseEvaluationDTO.getYearLevel(),
                baseEvaluationDTO.getSemester(),
                baseEvaluationDTO.getSchoolYear()
        );
    }
}
