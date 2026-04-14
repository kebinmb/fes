package com.faculty_evaluation_backend.fes.services.data.evaluation.strategies;

import com.faculty_evaluation_backend.fes.dto.evaluation.BaseEvaluationDTO;
import com.faculty_evaluation_backend.fes.repositories.authentication.StudentAccessCodeRepository;
import com.faculty_evaluation_backend.fes.repositories.evaluation.FacultyEvaluationScoreRepository;
import com.faculty_evaluation_backend.fes.repositories.primary.PrimaryStudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentEvaluationStrategy implements EvaluationStrategy {
    private final PrimaryStudentRepository primaryStudentRepository;
    private final StudentAccessCodeRepository studentAccessCodeRepository;
    private final FacultyEvaluationScoreRepository facultyEvaluationScoreRepository;

    @Override
    public void validate(BaseEvaluationDTO baseEvaluationDTO){
        primaryStudentRepository.findByStudentId(baseEvaluationDTO.getEvaluatorId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        studentAccessCodeRepository.findByStudentIdAndAccessCode(baseEvaluationDTO.getEvaluatorId(), baseEvaluationDTO.getAccessCode())
                .orElseThrow(() -> new RuntimeException("Invalid access code"));
    }

    @Override
    public boolean isDuplicate(BaseEvaluationDTO baseEvaluationDTO){
        return facultyEvaluationScoreRepository.existsByFacultyIdAndEvaluatorIdAndClassCodeAndSemesterAndSchoolYear(
                baseEvaluationDTO.getFacultyId(),
                baseEvaluationDTO.getEvaluatorId(),
                baseEvaluationDTO.getClassCode(),
                baseEvaluationDTO.getSemester(),
                baseEvaluationDTO.getSchoolYear()
        );
    }
}
