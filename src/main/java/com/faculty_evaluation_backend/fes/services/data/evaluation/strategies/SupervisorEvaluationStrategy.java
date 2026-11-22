package com.faculty_evaluation_backend.fes.services.data.evaluation.strategies;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.entities.authentication.UserAccounts;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupervisorEvaluationStrategy implements EvaluationStrategy {
    private final UserAccountsRepository userAccountsRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    @Override
    public void validate(BaseEvaluationDTO baseEvaluationDTO){
        Long id = Long.parseLong(baseEvaluationDTO.getEvaluatorId());
        UserAccounts supervisor = userAccountsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supervisor account not found"));

        validateCurrentPrincipal(supervisor);
    }

    private void validateCurrentPrincipal(UserAccounts supervisor) {
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

        boolean matchesUserId = principalName.equals(supervisor.getUserId().toString());
        boolean matchesUsername = supervisor.getUsername() != null && principalName.equalsIgnoreCase(supervisor.getUsername());

        if (!matchesUserId && !matchesUsername) {
            throw new AccessDeniedException("You are not authorized to submit evaluations for another supervisor.");
        }
    }

    @Override
    public boolean isDuplicate(BaseEvaluationDTO dto) {
        return facultyEvaluationScoreRepository
                .existsByFacultyIdAndEvaluatorIdAndClassCodeAndSubjectCodeAndYearLevelAndSemesterAndSchoolYear(
                        dto.getFacultyId(),
                        dto.getEvaluatorId(),
                        dto.getClassCode(),
                        dto.getSubjectCode(),
                        dto.getYearLevel(),
                        dto.getSemester(),
                        dto.getSchoolYear()
                );
    }
}
