package com.faculty_evaluation_backend.fes.services.data.evaluation.strategies;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.repositories.authentication.UserAccountsRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SupervisorEvaluationStrategy implements EvaluationStrategy {
    private final UserAccountsRepository userAccountsRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    @Override
    public void validate(BaseEvaluationDTO baseEvaluationDTO){
        Long id = Long.parseLong(baseEvaluationDTO.getEvaluatorId());
        userAccountsRepository.findById(id).orElseThrow(() -> new RuntimeException("Supervisor account not found"));
    }

    @Override
    public boolean isDuplicate(BaseEvaluationDTO dto) {
        return facultyEvaluationScoreRepository.existsByFacultyIdAndEvaluatorIdAndSubjectCodeAndSemesterAndSchoolYear(
                dto.getFacultyId(),
                dto.getEvaluatorId(),
                dto.getSubjectCode(),
                dto.getSemester(),
                dto.getSchoolYear()
        );
    }
}
